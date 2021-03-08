package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinHotSearch;
import com.jinguduo.spider.service.DouyinHotSearchService;

@Controller
@ResponseBody
public class DouyinHotSearchController {

	@Autowired
	private DouyinHotSearchService douyinHotSearchService;
	
	@PostMapping("/douyin/hot_searches")
	public String saveList(@RequestBody List<DouyinHotSearch> searches) {
		douyinHotSearchService.save(searches);
		return "OK";
	}
}
