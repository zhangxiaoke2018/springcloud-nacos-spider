package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.BaiduVideoLog;
import com.jinguduo.spider.service.BaiduVideoLogService;
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
@RequestMapping("/baidu_video_log")
public class BaiduVideoLogController {

    @Autowired
    private BaiduVideoLogService baiduVideoLogService;

    @PostMapping
    public BaiduVideoLog insertOrUpdate(@RequestBody BaiduVideoLog log){

        return baiduVideoLogService.insertOrUpdate(log);

    }





}
