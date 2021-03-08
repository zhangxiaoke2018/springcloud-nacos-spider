package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicDmzj;
import com.jinguduo.spider.service.ComicDmzjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comic_dmzj")
public class ComicDmzjController {

    @Autowired
    private ComicDmzjService comicDmzjService;

    @PostMapping
    public ComicDmzj save(@RequestBody ComicDmzj comicDmzj){

        ComicDmzj dmzj = comicDmzjService.insertOrUpdate(comicDmzj);

        return dmzj;
    }


}
