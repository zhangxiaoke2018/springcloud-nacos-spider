package com.jinguduo.spider.web;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.SougouWechatSearchText;
import com.jinguduo.spider.service.SougouWechatSearchService;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/6/28
 * Time:14:44
 */
@RestController
@RequestMapping("/sougouWechat_search_text")
public class SougouWechatSearchController {
    @Autowired
    private SougouWechatSearchService sougouWechatSearchService;

    @PostMapping
    public SougouWechatSearchText insert(@RequestBody SougouWechatSearchText text) throws IOException {
        sougouWechatSearchService.save(text);
        
        // 减少网络负载
        return new SougouWechatSearchText();
    }
}
