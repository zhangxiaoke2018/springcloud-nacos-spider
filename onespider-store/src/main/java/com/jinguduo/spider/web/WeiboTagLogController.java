package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.WeiboTagLog;
import com.jinguduo.spider.service.WeiboTagLogService;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Created by gsw on 2017/1/5.
 */
@Controller
@ResponseBody
@CommonsLog
public class WeiboTagLogController {

    @Autowired
    private WeiboTagLogService weiboTagLogService;

    @RequestMapping(value="/weibotaglog", method =RequestMethod.POST)
    public WeiboTagLog post(@RequestBody WeiboTagLog weiboTagLog) {
        
        if (weiboTagLog == null || weiboTagLog.getCode() == null) {
            log.warn(String.format("The WeiboTagLog is bad.[%s]", weiboTagLog.toString()));
            return weiboTagLog;
        }
        
        return this.weiboTagLogService.insert(weiboTagLog);
    }

}
