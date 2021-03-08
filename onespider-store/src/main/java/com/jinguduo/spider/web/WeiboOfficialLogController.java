package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.WeiboOfficialLog;
import com.jinguduo.spider.service.WeiboOfficialLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 15:27
 */
@RestController
@RequestMapping("/weibo_official_log")
public class WeiboOfficialLogController {

    @Autowired
    private WeiboOfficialLogService weiboOfficialLogService;

    @PostMapping
    public WeiboOfficialLog insertOrUpdate(@RequestBody WeiboOfficialLog log){

        return weiboOfficialLogService.insertOrUpdate(log);

    }





}
