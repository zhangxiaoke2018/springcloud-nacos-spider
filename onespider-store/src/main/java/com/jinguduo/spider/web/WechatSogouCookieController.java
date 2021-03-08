package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.WechatSogouCookie;
import com.jinguduo.spider.service.WechatSogouCookieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/4/30
 */
@RestController
@RequestMapping("/sogou_wechat/cookie")
public class WechatSogouCookieController {

    @Autowired
    WechatSogouCookieService service;

    @RequestMapping
    public Iterable<WechatSogouCookie> getAll() {

        return service.findAll();
    }

}
