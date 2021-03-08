package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicBodong;
import com.jinguduo.spider.service.ComicBodongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2018/9/10
 */
@RestController
@RequestMapping("/comic_bodong")
public class ComicBodongController {
    @Autowired
    private ComicBodongService service;

    @PostMapping
    public ComicBodong save(@RequestBody ComicBodong bodong) {

        return service.insertOrUpdate(bodong);

    }
}
