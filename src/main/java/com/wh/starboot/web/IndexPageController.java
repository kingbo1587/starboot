package com.wh.starboot.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexPageController {
	

	@Value("${hello.rivulet}")
	private String msg;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World222!" + msg;
	}

	@RequestMapping("hello")
	String first() {
		return "hello";
	}

	
}
