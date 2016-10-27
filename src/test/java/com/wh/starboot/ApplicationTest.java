package com.wh.starboot;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class ApplicationTest {

    @Test
    public void getReloadableValue() throws Exception {
        String version = Application.getReloadableValue("version");
        Assert.assertNotNull(version);
    }

}