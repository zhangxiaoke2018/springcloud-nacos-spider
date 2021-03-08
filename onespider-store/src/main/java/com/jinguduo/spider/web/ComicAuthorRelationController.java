package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.service.ComicAuthorRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/4/18
 */
@RestController
@RequestMapping("/r_comic_author")
public class ComicAuthorRelationController {
    @Autowired
    ComicAuthorRelationService service;


    @PostMapping
    public ComicAuthorRelation save(@RequestBody ComicAuthorRelation car) {

        return service.save(car);

    }
}
