package com.wh.starboot.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kingbo on 2016/11/1.
 */
public class SnowflakeTest {

    @Test
    public void next() throws Exception {
        Snowflake snowflake = new Snowflake(1);
        System.out.println(snowflake.next());
    }

}