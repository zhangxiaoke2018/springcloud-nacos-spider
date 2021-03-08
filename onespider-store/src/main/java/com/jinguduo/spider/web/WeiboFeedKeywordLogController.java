package com.jinguduo.spider.web;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.WeiboFeedKeywordLog;
import com.jinguduo.spider.service.WeiboFeedKeywordLogService;

@Controller
@ResponseBody
public class WeiboFeedKeywordLogController {

    @Autowired
    private WeiboFeedKeywordLogService weiboFeedKeywordLogService;
    
    @RequestMapping(value = "/weibo_feed_keyword_log", method = RequestMethod.POST)
    public boolean doPost(@RequestBody Collection<WeiboFeedKeywordLog> logs) {
        
        boolean r = weiboFeedKeywordLogService.save(logs);
        
        return r;
    }
}
