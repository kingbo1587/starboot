package com.wh.starboot.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

	@RequestMapping("index")
	String index(Model model) {
		model.addAttribute("name", "Coder");
		List<String> list = new ArrayList<>();
		list.add("storm");
		list.add("river");
		list.add("bread");
		model.addAttribute("projects", list);
		return "index";
	}

}
