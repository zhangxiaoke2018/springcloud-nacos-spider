package com.jinguduo.spider.web;

import com.jinguduo.spider.service.WechatArticleAdKeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 06/06/2017 10:36
 */
@RestController
@RequestMapping("/wechat_article_ad_keyword")
public class WechatArticleAdKeywordsController {

    @Autowired
    private WechatArticleAdKeywordService wechatArticleAdKeywordService;


    @GetMapping("/all")
    public Object findAll(){

        return wechatArticleAdKeywordService.findAll();

    }

    @GetMapping("/del/{id}")
    public Object del(@PathVariable Integer id){

        wechatArticleAdKeywordService.del(id);

        return "SUCCESS";
    }

    @GetMapping("/add/{keyword}")
    public Object add(@PathVariable String keyword){

        wechatArticleAdKeywordService.add(keyword);

        return "SUCCESS";
    }




}
