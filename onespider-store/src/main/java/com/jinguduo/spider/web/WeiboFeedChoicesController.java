package com.jinguduo.spider.web;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.WeiboFeedChoices;
import com.jinguduo.spider.service.WeiboFeedChoicesService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/03/2017 17:52
 */
@RestController
public class WeiboFeedChoicesController {

    @Autowired
    private WeiboFeedChoicesService weiboFeedChoicesService;

    @RequestMapping(value = "/weibo_feed_choice", method = RequestMethod.POST)
    public boolean doPost(@RequestBody Collection<WeiboFeedChoices> items) {

        return weiboFeedChoicesService.save(items);
    }



}
