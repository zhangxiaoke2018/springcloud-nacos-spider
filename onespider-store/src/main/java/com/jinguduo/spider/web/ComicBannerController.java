package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicBanner;
import com.jinguduo.spider.service.ComicBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/8/12
 */
@RestController
@RequestMapping("/comic_banner")
public class ComicBannerController {
    @Autowired
    private ComicBannerService service;

    @PostMapping
    public ComicBanner saveOrUpdate(@RequestBody ComicBanner banner) {

        return service.insertOrUpdate(banner);

    }
}
