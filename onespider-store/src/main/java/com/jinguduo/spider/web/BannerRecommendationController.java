package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.service.BannerRecommendationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/banner")
public class BannerRecommendationController {

    @Autowired
    private BannerRecommendationService bannerService;

    @RequestMapping(value="/save",method = RequestMethod.POST)
	public String saveBanner(@RequestBody BannerRecommendation banner) {
    	
    	try {
    		return bannerService.saveBanner(banner);
    	}catch(Exception e) {
    		return "Save Failed:"+banner.toString()+" e: "+e.getMessage();
    	}
	}
}
