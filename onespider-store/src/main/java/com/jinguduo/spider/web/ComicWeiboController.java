package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicWeibo;
import com.jinguduo.spider.service.ComicWeiboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comic_weibo")
public class ComicWeiboController {
    @Autowired
    private ComicWeiboService comicWeiboService;

    @PostMapping
    public ComicWeibo insertOrUpdate(@RequestBody ComicWeibo comicWeibo){
        ComicWeibo cw = comicWeiboService.insertOrUpdate(comicWeibo);
        return cw;
    }

}
