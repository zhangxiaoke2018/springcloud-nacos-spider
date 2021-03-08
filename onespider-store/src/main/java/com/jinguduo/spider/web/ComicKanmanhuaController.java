package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicKanmanhua;
import com.jinguduo.spider.service.ComicKanmanhuaService;
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
@RequestMapping("/comic_kanmanhua")
public class ComicKanmanhuaController {
    @Autowired
    private ComicKanmanhuaService comicKanmanhuaService;

    @PostMapping
    public ComicKanmanhua save(@RequestBody ComicKanmanhua kanmanhua) {

        ComicKanmanhua kan = comicKanmanhuaService.insertOrUpdate(kanmanhua);

        return kan;
    }
}
