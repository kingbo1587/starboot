package com.wh.starboot.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wh.starboot.model.StudentBean;
import com.wh.starboot.service.StudentService;

@Controller
@RequestMapping("student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @RequestMapping(value = "get", method = RequestMethod.GET)
    @ResponseBody
    StudentBean get(String studentId) {
        logger.info("get|studentId:{}", studentId);
        return studentService.get(studentId);
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    @ResponseBody
    Integer save(@RequestBody StudentBean studentBean) {
        logger.info("get|studentBean:{}", studentBean);
        return studentService.save(studentBean);
    }

    @RequestMapping(value = "getBaidu", method = RequestMethod.GET)
    @ResponseBody
    String getBaidu() {
        studentService.getBaidu();
        return "1";
    }

    @RequestMapping(value = "sendMq", method = RequestMethod.GET)
    @ResponseBody
    String sendMq(String message) {
        studentService.sendMq(message);
        return "1";
    }

}
