package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.DoubanBook;
import com.jinguduo.spider.service.DoubanBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2020/1/15
 */
@RestController
@RequestMapping("/douban_book")
public class DoubanBookController {
    @Autowired
    DoubanBookService service;

    @PostMapping
    public DoubanBook saveOrUpdate(@RequestBody DoubanBook doubanBook){
        return service.saveOrUpdate(doubanBook);
    }
}
