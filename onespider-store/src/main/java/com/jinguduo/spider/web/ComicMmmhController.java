package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicMmmh;
import com.jinguduo.spider.service.ComicMmmhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/10/20
 * Time:11:36
 */
@RestController
@RequestMapping("/comic_mmmh")
public class ComicMmmhController {
    @Autowired
    private ComicMmmhService comicMmmhService;

    @PostMapping
    public ComicMmmh save(@RequestBody ComicMmmh comicMmmh){

        ComicMmmh mmmh = comicMmmhService.insertOrUpdate(comicMmmh);

        return mmmh;
    }
}
