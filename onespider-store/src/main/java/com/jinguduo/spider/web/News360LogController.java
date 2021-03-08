package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.News360Log;
import com.jinguduo.spider.service.News360LogService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 15:27
 */
@RestController
@RequestMapping("/news_360_log")
public class News360LogController {

    @Autowired
    private News360LogService news360LogService;

    @PostMapping
    public News360Log insertOrUpdate(@RequestBody News360Log newsLog){

        return news360LogService.insertOrUpdate(newsLog);

    }





}
