package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.service.FictionIncomeLogsService;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fiction_income")
@CommonsLog
public class FictionIncomeLogsController {

    @Autowired
    private FictionIncomeLogsService fictionIncomeLogsService;

    @RequestMapping(method = RequestMethod.POST)
	public String insert(@RequestBody FictionIncomeLogs incomeLog) {
    	fictionIncomeLogsService.insert(incomeLog);
		return null;
	}
    
}
