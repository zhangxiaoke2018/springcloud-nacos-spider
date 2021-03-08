package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.service.AdLinkedVideoInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
public class AdLinkedVideoInfoController {
	
	@Autowired
	private AdLinkedVideoInfoService adLinkedVideoInfoService;

	@RequestMapping(path = "/ad_link_video", method = RequestMethod.POST)
	public String postLinkedVideoInfo(@RequestBody AdLinkedVideoInfos adLinkedVideoInfos) {
		adLinkedVideoInfoService.insert(adLinkedVideoInfos);
		return null;
	}

}
