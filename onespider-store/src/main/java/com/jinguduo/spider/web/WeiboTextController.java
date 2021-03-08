package com.jinguduo.spider.web;

import java.io.IOException;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.WeiboText;
import com.jinguduo.spider.service.WeiboTextService;

@Controller
@ResponseBody
@CommonsLog
public class WeiboTextController {
    
    @Autowired
    private WeiboTextService weiboTextService;

    @RequestMapping(path = "/weibo_texts", method = RequestMethod.POST)
    public boolean post(@RequestBody WeiboText weiboTexts) {
        boolean r = false;
        
        try {
            weiboTextService.save(weiboTexts);
            r = true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return r;
    }
}
