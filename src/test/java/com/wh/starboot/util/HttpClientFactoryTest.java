package com.wh.starboot.util;

import com.wh.starboot.Application;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by kingbo on 2016/10/27.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class, loader = SpringBootContextLoader.class)
@SpringBootTest()
public class HttpClientFactoryTest {

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Test
    public void execute() throws Exception {
        String requestId = "haha";
        HttpGet httpGet = new HttpGet("http://www.baidu.com");
        HttpData httpData = httpClientFactory.execute(requestId, httpGet);
        System.out.println(httpData);
    }

}