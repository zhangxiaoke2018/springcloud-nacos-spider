package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.FictionChapters;
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformFavorite;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.data.table.FictionPlatformRecommend;
import com.jinguduo.spider.service.FictionAttributeService;

@RestController
public class FictionAttributeController {

    @Autowired
    private FictionAttributeService fictionAttributeService;

    @PostMapping("/fiction_attribute/click")
	public String click(@RequestBody FictionPlatformClick item) {
		return (null!=fictionAttributeService.save(item))?"SUCCESS":"FAIL";
	}
    
    @PostMapping("/fiction_attribute/favorite")
	public String favorite(@RequestBody FictionPlatformFavorite item) {
		return (null!=fictionAttributeService.save(item))?"SUCCESS":"FAIL";
	}
    
    @PostMapping("/fiction_attribute/recommend")
   	public String recommend(@RequestBody FictionPlatformRecommend item) {
   		return (null!=fictionAttributeService.save(item))?"SUCCESS":"FAIL";
   	}
    
    @PostMapping("/fiction_attribute/rate")
	public String rate(@RequestBody FictionPlatformRate item) {
		return (null!=fictionAttributeService.save(item))?"SUCCESS":"FAIL";
	}
    
    @PostMapping("/fiction_attribute/chapter")
   	public String chapter(@RequestBody FictionChapters item) {
   		return (null!=fictionAttributeService.save(item))?"SUCCESS":"FAIL";
   	}
}
