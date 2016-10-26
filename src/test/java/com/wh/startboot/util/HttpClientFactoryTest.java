package com.wh.startboot.util;

import com.wh.starboot.Application;
import com.wh.starboot.util.HttpClientFactory;
import com.wh.starboot.util.HttpData;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;

/**
 * Created by  on 2016/10/22.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class, loader = SpringBootContextLoader.class)
@SpringBootTest()
public class HttpClientFactoryTest {

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Test
    public void test1() {
        System.out.println("I am test1");
    }

    @Test
    public void testGet() {
        String requestId = "haha";
        HttpGet httpGet = new HttpGet("http://www.baidu.com");
        HttpData httpData = httpClientFactory.execute(requestId, httpGet);
        System.out.println(httpData);
    }
}
