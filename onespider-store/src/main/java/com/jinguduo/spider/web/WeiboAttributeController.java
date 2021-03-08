package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.WeiboAttribute;
import com.jinguduo.spider.service.WeiboAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 20/04/2017 14:29
 */
@RestController
@RequestMapping("/weibo_attr")
public class WeiboAttributeController {

    @Autowired
    private WeiboAttributeService weiboAttributeService;

    @PostMapping
    public WeiboAttribute insertOrUpdate(@RequestBody WeiboAttribute weiboAttribute){

        return weiboAttributeService.insertOrUpdate(weiboAttribute);
    }



}
