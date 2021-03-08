package com.jinguduo.spider.web;

import com.jinguduo.spider.service.CoreKeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 10/11/2017 09:54
 */
@RestController
@RequestMapping("/core_keyword")
public class CoreKeywordController {

    @Autowired
    private CoreKeywordService coreKeywordService;

    @GetMapping("/all")
    public Object findAll(){
        return coreKeywordService.findAll();
    }

    @GetMapping("/alias/all")
    public Object findAliasAll() {
        return coreKeywordService.findAliasAll();
    }

}
