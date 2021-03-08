package com.jinguduo.spider.web;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.WechatArticleKeywordLog;
import com.jinguduo.spider.service.WechatArticleKeywordLogService;

@Controller
@ResponseBody
public class WechatArticleKeywordLogController {

    @Autowired
    private WechatArticleKeywordLogService wechatArticleKeywordLogService;
    
    @RequestMapping(value = "/wechat_article_keyword_log", method = RequestMethod.POST)
    public boolean doPost(@RequestBody Collection<WechatArticleKeywordLog> logs) {
        
        boolean r = wechatArticleKeywordLogService.save(logs);
        
        return r;
    }
}
