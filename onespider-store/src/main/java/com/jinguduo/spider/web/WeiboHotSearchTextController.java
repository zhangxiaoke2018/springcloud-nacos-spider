package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.text.WeiboHotSearchText;
import com.jinguduo.spider.service.WeiboHotSearchTextService;

@RestController
public class WeiboHotSearchTextController {

	@Autowired
	private WeiboHotSearchTextService weiboHotSearchTextService;

	@RequestMapping(path = "/weibo_hot_search_texts", method = RequestMethod.POST)
	public String add(@RequestBody WeiboHotSearchText weiboHotSearchTexts) {
		weiboHotSearchTextService.save(weiboHotSearchTexts);
		return "OK";
	}
}
