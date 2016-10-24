package com.wh.starboot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by 掌众 on 2016/10/19.
 */
@Component
public class HttpClientFactory {

    private final static Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);

    // httpclient连接池，基于tokenId分配httpclient
    // 每一个新的请求都是一个新的client
    // cookie存入redis
    // 灵活支持请求数据，请求方式

    private static final List<Header> defaultHeaders = new ArrayList<>(Arrays.asList(
            new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
            new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"),
            new BasicHeader("Accept-Encoding", "gzip, deflate"),
            new BasicHeader("Connection", "keep-alive"),
            new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36")
    ));

    private PoolingHttpClientConnectionManager connManager;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void init() {
        logger.info("HttpClientFactory|init");
        connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(200);
        connManager.setMaxTotal(400);
        connManager.setValidateAfterInactivity(2 * 60 * 1000);
    }

    private void insertCookieStore(String requestId, BasicCookieStore basicCookieStore, long timeout) {
        String redisKey = generateRedisKey(requestId);
        String json = null;
        try {
            json = new ObjectMapper().writeValueAsString(basicCookieStore);
        } catch (JsonProcessingException e) {
            logger.error("write as json exception|requestId:{}", requestId, e);
        }
        if (json == null) {
            return;
        }
        if (timeout >= 0) {
            redisTemplate.opsForValue().set(redisKey, json, 30, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().set(redisKey, json);
        }
    }

    public void updateCookieStore(String requestId, BasicCookieStore basicCookieStore) {
        insertCookieStore(requestId, basicCookieStore, -1);
    }

    private BasicCookieStore getCookieStore(String requestId) {
        String json = redisTemplate.opsForValue().get(generateRedisKey(requestId));
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        BasicCookieStore cookieStore = null;
        try {
            cookieStore = objectMapper.readValue(json, BasicCookieStore.class);
        } catch (IOException e) {
            logger.error("read json value exception|requestId:{}", requestId, e);
        }
        return cookieStore;
    }

    public HttpData execute(String requestId, HttpRequestBase hrb) {
        BasicCookieStore cookieStore = null;
        if (requestId != null && !"".equals(requestId)) {
            cookieStore = getCookieStore(requestId);
        }
        HttpClientBuilder builder = HttpClients.custom();
        builder.setConnectionManager(connManager);
        builder.setDefaultHeaders(defaultHeaders);
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        CloseableHttpClient httpClient = builder.build();
        HttpData httpData = new HttpData();
        try {
            HttpResponse httpResponse = httpClient.execute(hrb);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String data = EntityUtils.toString(httpResponse.getEntity());
            httpData.setStatusCode(statusCode);
            httpData.setData(data);
            httpData.setCookieStore(cookieStore);
            if (cookieStore != null) {
                // 请求完更新cookieStore
                updateCookieStore(requestId, cookieStore);
            }
        } catch (Exception e) {
            logger.error("http request excetpion|requestId:{}", requestId, e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("colse httpclient excetpion|requestId:{}", requestId, e);
                }
            }
            if (hrb != null) {
                hrb.releaseConnection();
            }
        }
        return httpData;
    }

    private static String generateRedisKey(String requestId) {
        return "httpclient_factory_" + requestId;
    }


}
