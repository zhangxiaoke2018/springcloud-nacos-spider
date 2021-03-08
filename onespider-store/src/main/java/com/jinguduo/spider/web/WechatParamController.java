package com.jinguduo.spider.web;

import com.jinguduo.spider.service.WechatParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @DATE 2018/10/11 11:11 AM
 */
@RestController
public class WechatParamController {

    @Autowired
    private WechatParamService wechatParamService;


    @GetMapping("/wechat_param/key/{key}")
    public String findValue(@PathVariable String key){

        return wechatParamService.findValue(key);
    }

    @GetMapping("/wechat_param/update")
    public String update(@RequestParam String key, @RequestParam String value){

        return wechatParamService.update(key, value);

    }



}
