package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.data.table.SpiderSetting;
import com.jinguduo.spider.service.SpiderSettingService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@ResponseBody
@RestController
@RequestMapping("/spider_setting")
public class SpiderSettingController {

    @Autowired
    private SpiderSettingService settingService;

    @RequestMapping(method = RequestMethod.GET)
    private SpiderSetting getSetting(@RequestParam String domain){

        return settingService.findOne(domain);
    }

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    private Object getAllSetting(){

        return settingService.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    private Object insertOrUpdate(@RequestBody SpiderSetting setting){
        return settingService.insertOrUpdate(setting);
    }

    @RequestMapping(value = "/getlist",method = RequestMethod.GET)
    public Paginator<SpiderSetting> doList(
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "30", required = false) Integer size){
        Paginator<SpiderSetting> p;
        if (StringUtils.hasText(domain)) {
            List<SpiderSetting> list = Lists.newArrayList();
            SpiderSetting setting = settingService.findByDomain(domain);
            if (setting != null) {
                list.add(setting);
            }
            p = new Paginator<>(1,size,list.size());
            p.setEntites(list);
        } else {
            Page<SpiderSetting> pages = settingService.findSettingPage(page-1, size);
            p = new Paginator<>(page, size);
            p.setPageCount(pages.getTotalPages());
            p.setEntites(pages.getContent());
        }
        return p;
    }
    
    @RequestMapping(value = "/getOne",method = RequestMethod.GET)
    public Object getOneSetting(@RequestParam("id") String id){
        Assert.notNull(id);
        return settingService.findOneById(Integer.valueOf(id));
    }
    
    @RequestMapping(value = "/modify",method = RequestMethod.GET)
    public Object modifySpiderSetting(@ModelAttribute SpiderSetting setting){
        settingService.insertOrUpdate(setting);
        return "SUCCESS";
    }
}
