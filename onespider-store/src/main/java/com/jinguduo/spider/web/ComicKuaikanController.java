package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.service.ComicKuaiKanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2017/5/5.
 */
@RestController
@RequestMapping("/comic_kuaikan")
public class ComicKuaikanController {

    @Autowired
    private ComicKuaiKanService kuaiKanService;


    @PostMapping
    public ComicKuaiKan insertOrUpdate(@RequestBody ComicKuaiKan logs) {
        return kuaiKanService.save(logs);

    }

}
