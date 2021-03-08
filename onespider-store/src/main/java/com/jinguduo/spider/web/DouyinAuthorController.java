package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinAuthor;
import com.jinguduo.spider.service.DouyinAuthorService;

@Controller
@ResponseBody
public class DouyinAuthorController {

	@Autowired
	private DouyinAuthorService douyinAuthorService;
	
	@PostMapping("/douyin/authors")
	public String saveList(@RequestBody List<DouyinAuthor> authors) {
		douyinAuthorService.save(authors);
		return "OK";
	}
}
