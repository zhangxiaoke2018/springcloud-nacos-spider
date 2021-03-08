package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.WeiboIndexHourLog;
import com.jinguduo.spider.service.WeiboIndexHourLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeiboIndexHourLogController {

    @Autowired
    private WeiboIndexHourLogService weiboIndexHourLogService;

    @RequestMapping(path = "/weibo_index_hour_log", method = RequestMethod.POST)
    public boolean saveCommentLog(@RequestBody WeiboIndexHourLog indexHourLog) {

        return null != weiboIndexHourLogService.insertOrUpdate(indexHourLog) ? true : false;
    }

}
