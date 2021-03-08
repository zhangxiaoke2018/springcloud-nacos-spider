package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinVideoStatistic;
import com.jinguduo.spider.service.DouyinVideoStatisticService;

@Controller
@ResponseBody
public class DouyinVideoStatisticController {

	@Autowired
	private DouyinVideoStatisticService douyinVideoStatisticService;
	
	@PostMapping("/douyin/video_statistics")
	public String saveList(@RequestBody List<DouyinVideoStatistic> statistics) {
		douyinVideoStatisticService.save(statistics);
		return "OK";
	}
}
