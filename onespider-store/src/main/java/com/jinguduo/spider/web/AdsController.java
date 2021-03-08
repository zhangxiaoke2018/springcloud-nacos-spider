package com.jinguduo.spider.web;

import com.jinguduo.spider.service.AdsService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 06/12/2017 18:51
 */
@Deprecated
@RestController
@RequestMapping("/ads")
@CommonsLog
public class AdsController {

    @Autowired
    private AdsService adsService;


    @GetMapping("/all")
    public Object findAll(){

        return adsService.findAll();

    }
    @GetMapping("/page")
    public Object page(@RequestParam Integer page, @RequestParam Integer size){

        return adsService.page(page, size);

    }

    @GetMapping("/not_all")
    public Object findAllNoName(){

        return adsService.findAllNoName();
    }
    @GetMapping("/edit")
    public Object updateName(@RequestParam String name, @RequestParam Integer id){

        return adsService.edit(name, id);
    }




}
