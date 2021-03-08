package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.WeiboFeedKeywordTag;
import com.jinguduo.spider.service.WeiboFeedKeywordTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 08/08/2017 10:59
 */
@RestController
@RequestMapping("/weibo_feed_keyword_tags")
public class WeiboFeedKeywordTagController {

    @Autowired
    private WeiboFeedKeywordTagService weiboFeedKeywordTagService;

    @PostMapping
    public String save(@RequestBody Collection<WeiboFeedKeywordTag> weiboFeedKeywordTag){

        weiboFeedKeywordTagService.save(weiboFeedKeywordTag);

        return "SUCCESS";

    }

    @RequestMapping("/one")
    public String savePojo(@RequestBody WeiboFeedKeywordTag weiboFeedKeywordTag){

        weiboFeedKeywordTagService.save(weiboFeedKeywordTag);

        return "SUCCESS";

    }


}
