package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.service.FictionCommentLogsService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fiction_comment")
@CommonsLog
public class FictionCommentLogsController {

    @Autowired
    private FictionCommentLogsService fictionCommentLogsService;

    @RequestMapping(method = RequestMethod.POST)
	public String insert(@RequestBody FictionCommentLogs commentLogs) {
    	fictionCommentLogsService.insert(commentLogs);
		return null;
	}
    
}
