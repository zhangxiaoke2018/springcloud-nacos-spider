package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicZymk;
import com.jinguduo.spider.service.ComicZymkService;
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
@RequestMapping("/comic_zymk")
public class ComicZymkController {
    @Autowired
    private ComicZymkService comicZymkService;

    @PostMapping
    public ComicZymk save(@RequestBody ComicZymk comicZymk){

        ComicZymk zymk = comicZymkService.insertOrUpdate(comicZymk);

        return zymk;
    }
}
