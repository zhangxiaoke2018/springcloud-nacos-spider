package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicTengxun;
import com.jinguduo.spider.service.ComicTengxunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2017/5/5.
 */
@RestController
@RequestMapping("/comic_tengxun")
public class ComicTengxunController {

    @Autowired
    private ComicTengxunService service;


    @PostMapping
    public ComicTengxun insertOrUpdate(@RequestBody ComicTengxun logs) {
        return service.saveOrUpdate(logs);
    }

}
