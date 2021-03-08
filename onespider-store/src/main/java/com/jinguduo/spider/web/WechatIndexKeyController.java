package com.jinguduo.spider.web;

import com.jinguduo.spider.service.WechatIndexKeyService;
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
public class WechatIndexKeyController {

    @Autowired
    private WechatIndexKeyService wechatIndexKeyService;


    @GetMapping("/wechat_index_key/{openId}/{searchKey}")
    public Object findValue(@PathVariable String openId, @PathVariable String searchKey){

        return wechatIndexKeyService.save(openId, searchKey);
    }



}
