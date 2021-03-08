package com.jinguduo.spider.web;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.SougouWechatArticleText;
import com.jinguduo.spider.service.SougouWechatArticleService;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/6/28
 * Time:14:46
 */
@RestController
@RequestMapping("/sougouWechat_article_text")
public class SougouWechatArticleController {
    @Autowired
    private SougouWechatArticleService sougouWechatArticleService;

    @PostMapping
    public SougouWechatArticleText insert(@RequestBody SougouWechatArticleText text) throws IOException {
        sougouWechatArticleService.save(text);
        return new SougouWechatArticleText();
    }
}
