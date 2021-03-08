package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinVideoDigg;
import com.jinguduo.spider.service.DouyinVideoDiggService;

@Controller
@ResponseBody
public class DouyinVideoDiggController {

	@Autowired
	private DouyinVideoDiggService douyinVideoDiggService;
	
	@PostMapping("/douyin/video_diggs")
	public String saveList(@RequestBody List<DouyinVideoDigg> diggs) {
		douyinVideoDiggService.save(diggs);
		return "OK";
	}
}
