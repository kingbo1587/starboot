package com.wh.starboot.service.impl;

import com.wh.starboot.Application;
import com.wh.starboot.service.StudentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by kingbo on 2016/11/28.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class, loader = SpringBootContextLoader.class)
@SpringBootTest()
public class StudentServiceImplTest {

    @Autowired
    private StudentService studentService;

    @Test
    public void sayHi() throws Exception {
        studentService.sayHi("======>lalala");
    }

}