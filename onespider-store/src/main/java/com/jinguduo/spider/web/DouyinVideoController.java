package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinVideo;
import com.jinguduo.spider.service.DouyinVideoService;

@Controller
@ResponseBody
public class DouyinVideoController {

	@Autowired
	private DouyinVideoService douyinVideoService;
	
	@PostMapping("/douyin/videos")
	public String saveList(@RequestBody List<DouyinVideo> videos) {
		douyinVideoService.save(videos);
		return "OK";
	}
}
