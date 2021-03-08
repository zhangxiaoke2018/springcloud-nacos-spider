package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.RelationKeywords;
import com.jinguduo.spider.service.RelationKeywordsService;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月6日 下午5:10:20
 *
 */
@RestController
@RequestMapping("rkeywords")
public class RelationKeywordsController {

    @Autowired
    private RelationKeywordsService relationKeywordsService;

    @RequestMapping(value = "update",method = RequestMethod.POST)
    public RelationKeywords updateRKeywords(@RequestBody RelationKeywords rkeyword){
        return relationKeywordsService.updateRKeywords(rkeyword);
    }
}
