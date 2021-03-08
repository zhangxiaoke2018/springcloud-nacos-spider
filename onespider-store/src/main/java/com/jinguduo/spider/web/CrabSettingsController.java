package com.jinguduo.spider.web;

import com.jinguduo.spider.service.CrabSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/05/2017 16:10
 */
@RestController
@RequestMapping("/crab_settings")
public class CrabSettingsController {

    @Autowired
    private CrabSettingsService crabSettingsService;

    //supervisor缓存
    @GetMapping(value = "/all")
    public Object getAllSeedByTime(@RequestParam("load_time") Long loadTime){
        return crabSettingsService.find(loadTime);
    }


}
