package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.AdLogs;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.service.AdLogService;
import com.jinguduo.spider.service.ShowLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
public class AdLogController {
	
	@Autowired
	private AdLogService adLogService;

	@RequestMapping(path = "/ad_log", method = RequestMethod.POST)
	public String postShowLog(@RequestBody AdLogs adLogs) {
		adLogService.insert(adLogs);
		return null;
	}

}
