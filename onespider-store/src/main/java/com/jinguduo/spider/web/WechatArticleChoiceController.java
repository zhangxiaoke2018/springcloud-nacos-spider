package com.jinguduo.spider.web;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.WechatArticleChoice;
import com.jinguduo.spider.service.WechatArticleChoiceService;

@Controller
@ResponseBody
public class WechatArticleChoiceController {

    @Autowired
    private WechatArticleChoiceService wechatArticleChoiceService;
    
    @RequestMapping(value = "/wechat_article_choice", method = RequestMethod.POST)
    public boolean doPost(@RequestBody Collection<WechatArticleChoice> items) {
        
        return wechatArticleChoiceService.save(items);
    }
    @GetMapping("/wechat_article_choice/no_read_count")
    public Set<String> findNoReadCount(@RequestParam(required = false) String day){

        return wechatArticleChoiceService.findNoReadCount(day);
    }
}
