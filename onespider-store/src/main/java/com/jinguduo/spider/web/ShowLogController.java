package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.service.ShowLogService;

@RestController
public class ShowLogController {
	
	@Autowired
	private ShowLogService showLogService;

	@RequestMapping(path = "/show_log", method = RequestMethod.POST)
	public String postShowLog(@RequestBody ShowLog showLog) {
		showLogService.insert(showLog);
		return null;
	}
	@RequestMapping(value = "show_log",method = RequestMethod.GET)
	public Object get(@RequestParam("code") String code){
		return showLogService.find(code);
	}

	@RequestMapping(value = "show_log/times",method = RequestMethod.GET)
	public Object getShowLogTime(@RequestParam("name") String name,@RequestParam("startDate")String startDate , @RequestParam("endDate")String endDate){
		return showLogService.find(name,startDate,endDate);
	}

	@GetMapping("/show_log/exact")
	public Object getShowLogExact(@RequestParam("type") Integer type,@RequestParam("name") String name,@RequestParam("start")String start, @RequestParam("end")String end){
	    return showLogService.findExact(type,name,start,end);
	}
}
