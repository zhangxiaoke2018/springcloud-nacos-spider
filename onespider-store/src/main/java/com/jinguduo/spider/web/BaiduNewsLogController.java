package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.BaiduNewsLog;
import com.jinguduo.spider.service.BaiduNewsLogService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 15:27
 */
@RestController
@RequestMapping("/baidu_news_log")
public class BaiduNewsLogController {

    @Autowired
    private BaiduNewsLogService baiduNewsLogService;

    @PostMapping
    public BaiduNewsLog insertOrUpdate(@RequestBody BaiduNewsLog newsLog){

        return baiduNewsLogService.insertOrUpdate(newsLog);

    }





}
