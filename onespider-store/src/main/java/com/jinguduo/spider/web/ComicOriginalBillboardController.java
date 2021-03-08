package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicOriginalBillboard;
import com.jinguduo.spider.service.ComicOriginalBillboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/5/29
 */
@RestController
@RequestMapping("/comic_original_billboard")
public class ComicOriginalBillboardController {

    @Autowired
    private ComicOriginalBillboardService service;


    @PostMapping
    public ComicOriginalBillboard insertOrUpdate(@RequestBody ComicOriginalBillboard billboard) {
        return service.insertOrUpdate(billboard);
    }


}
