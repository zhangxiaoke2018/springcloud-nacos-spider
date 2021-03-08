package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicBestSellingRank;
import com.jinguduo.spider.service.ComicBestSellingRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/4/17
 */
@RestController
@RequestMapping("/comic_best_selling_rank")
public class ComicBestSellingRankController {

    @Autowired
    public ComicBestSellingRankService service;

    @PostMapping
    public ComicBestSellingRank save(@RequestBody ComicBestSellingRank cbsr) {

        return service.save(cbsr);

    }
}
