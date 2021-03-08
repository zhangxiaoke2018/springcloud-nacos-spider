package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.service.FictionsService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fiction_meta")
@Slf4j
public class FictionsController {

    @Autowired
    private FictionsService fictionsService;

    @RequestMapping(method = RequestMethod.POST)
	public String insert(@RequestBody Fiction fiction) {
    	
    	try {
    	fictionsService.insert(fiction);
    	}catch(Exception e) {
    		log.info("insert fiction error:{} ,exception: {}",fiction,e.getClass().getName());
    	}
		return null;
	}
    
}
