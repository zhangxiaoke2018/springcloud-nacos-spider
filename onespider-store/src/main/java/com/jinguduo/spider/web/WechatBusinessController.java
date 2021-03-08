package com.jinguduo.spider.web;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.WechatBusiness;
import com.jinguduo.spider.service.WechatBusinessService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 14/03/2017 11:12 AM
 */
@RequestMapping("/wechat_business")
@RestController
@CommonsLog
public class WechatBusinessController {


    @Autowired
    private WechatBusinessService majorWechatService;


    @GetMapping("/all")
    public Object findAll(){

        return majorWechatService.findAll();
    }

    @GetMapping("/greatest/all")
    public Object allGreatest(){

        return majorWechatService.findAllGreatest();
    }



    @PostMapping
    public Object insertOrUpdate(@RequestBody WechatBusiness wechat){

        try {
            majorWechatService.insertOrUpdate(wechat);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return "FAILURE";
        }
        return "SUCCESS";

    }



}
