package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicBilibili;
import com.jinguduo.spider.service.ComicBilibiliService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2018/9/10
 */
@RestController
@RequestMapping("/comic_bilibili")
public class ComicBilibiliController {
    @Autowired
    private ComicBilibiliService service;

    @PostMapping
    public ComicBilibili save(@RequestBody ComicBilibili blbl) {

        return service.insertOrUpdate(blbl);

    }
}
