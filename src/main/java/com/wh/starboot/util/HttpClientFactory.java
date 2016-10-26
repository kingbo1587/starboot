package com.wh.starboot.util;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * httpClient请求工具类,将cookie仓库保存到redis，便于保存cookie
 */
@Component
public class HttpClientFactory {

    private final static Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);

    @Value("${proxyserver.host}")
    private String proxyHost;
    @Value("${proxyserver.port}")
    private int proxyPort;
    @Value("${isProxy}")
    private boolean isProxy;

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    private static final List<Header> defaultHeaders = new ArrayList<>(Arrays.asList(
            new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
            new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"),
            new BasicHeader("Accept-Encoding", "gzip, deflate"),
            new BasicHeader("Connection", "keep-alive"),
            new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36")
    ));
    private PoolingHttpClientConnectionManager connManager;


    @PostConstruct
    private void init() {
        logger.info("HttpClientFactory|init");
        connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(200);
        connManager.setMaxTotal(400);
        connManager.setValidateAfterInactivity(5 * 60 * 1000);
    }

    /**
     * 创建http客户端
     *
     * @param cookieStore cookie仓库
     * @return http客户端
     */
    private CloseableHttpClient create(BasicCookieStore cookieStore) {
        HttpClientBuilder builder = HttpClients.custom();
        builder.setConnectionManager(connManager);
        builder.setDefaultHeaders(defaultHeaders);
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        if (isProxy) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            builder.setProxy(proxy);
        }
        return builder.build();
    }

    /**
     * 执行请求
     *
     * @param requestId 请求标号
     * @param hrb       http请求
     * @return 响应数据
     */
    public HttpData execute(String requestId, HttpRequestBase hrb) {
        BasicCookieStore cookieStore = null;
        boolean saveCookie = false;
        boolean requestFirst = false;
        if (requestId != null && !"".equals(requestId)) {
            cookieStore = getCookieStore(requestId);
            saveCookie = true;
            if (cookieStore == null) {
                requestFirst = true;
                cookieStore = new BasicCookieStore();
            }
        }
        CloseableHttpClient httpClient = create(cookieStore);
        HttpData httpData = new HttpData();
        try {
            HttpResponse httpResponse = httpClient.execute(hrb);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String data = EntityUtils.toString(httpResponse.getEntity());
            httpData.setStatusCode(statusCode);
            httpData.setData(data);
            httpData.setCookieStore(cookieStore);
            if (saveCookie) {
                if (requestFirst) {
                    // 将cookie仓库保存到redis
                    saveCookieStore(requestId, cookieStore, 30);
                } else {
                    // 请求完更新cookieStore
                    updateCookieStore(requestId, cookieStore);
                }
            }
        } catch (Exception e) {
            logger.error("http request excetpion|requestId:{}", requestId, e);
        } finally {
            if (hrb != null) {
                hrb.releaseConnection();
            }
        }
        return httpData;
    }

    /**
     * 下载图片
     *
     * @param requestId 请求编号
     * @param url       图片地址
     * @return 图片字节数组
     */
    public byte[] downloadImage(String requestId, String url) {
        byte[] bytes = null;
        BasicCookieStore cookieStore = null;
        boolean saveCookie = false;
        boolean requestFirst = false;
        if (requestId != null && !"".equals(requestId)) {
            cookieStore = getCookieStore(requestId);
            saveCookie = true;
            if (cookieStore == null) {
                requestFirst = true;
                cookieStore = new BasicCookieStore();
            }
        }
        CloseableHttpClient httpClient = create(cookieStore);
        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            bytes = EntityUtils.toByteArray(httpResponse.getEntity());
            if (saveCookie) {
                if (requestFirst) {
                    // 将cookie仓库保存到redis
                    saveCookieStore(requestId, cookieStore, 30);
                } else {
                    // 请求完更新cookieStore
                    updateCookieStore(requestId, cookieStore);
                }
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
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return bytes;
    }

    /**
     * 序列化对象
     *
     * @param object 对象
     * @return 对象字节数组
     */
    private static byte[] serialize(Object object) {
        try {
            //序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("serialize exception", e);
        }
        return null;
    }

    /**
     * 反序列化
     *
     * @param bytes 对象字节数组
     * @param <T>   返回类型
     * @return 返回类
     */
    private static <T> T deserialize(byte[] bytes) {
        try {
            //反序列化
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            logger.error("deserialize exception", e);
        }
        return null;
    }

    /**
     * 生成redis的键
     *
     * @param requestId 请求编号
     * @return redis的键
     */
    private static String generateRedisKey(String requestId) {
        return "httpclient_factory_generateRedisKey_" + requestId;
    }

    /**
     * 保存cookie仓库
     *
     * @param requestId   请求编号
     * @param cookieStore cookie仓库
     * @param timeout     redis数据超时时间
     */
    private void saveCookieStore(String requestId, BasicCookieStore cookieStore, long timeout) {
        String redisKey = generateRedisKey(requestId);
        byte[] bytes = serialize(cookieStore);
        if (timeout >= 0) {
            redisTemplate.opsForValue().set(redisKey, bytes, timeout, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().set(redisKey, bytes);
        }
    }

    /**
     * 更新cookie仓库
     *
     * @param requestId   请求编号
     * @param cookieStore cookie仓库
     */
    public void updateCookieStore(String requestId, BasicCookieStore cookieStore) {
        saveCookieStore(requestId, cookieStore, -1);
    }

    /**
     * 根据请求编号查找cookie仓库
     *
     * @param requestId 请求编号
     * @return cookie仓库
     */
    private BasicCookieStore getCookieStore(String requestId) {
        String redisKey = generateRedisKey(requestId);
        byte[] bytes = redisTemplate.opsForValue().get(redisKey);
        if (bytes == null) {
            return null;
        }
        return deserialize(bytes);
    }

}
