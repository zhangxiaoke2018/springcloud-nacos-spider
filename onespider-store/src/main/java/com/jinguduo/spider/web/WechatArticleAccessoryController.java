package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.WechatArticleAccessory;
import com.jinguduo.spider.service.WechatArticleAccessoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @DATE 2018/10/12 4:00 PM
 */
@RestController
public class WechatArticleAccessoryController {

    @Autowired
    private WechatArticleAccessoryService wechatArticleAccessoryService;

    @RequestMapping(path = "/wechat_article_accessory", method = RequestMethod.POST)
    public String postWechatArticleAccessory(@RequestBody WechatArticleAccessory wechatArticleAccessory) {
        wechatArticleAccessoryService.insert(wechatArticleAccessory);
        return null;
    }

}
