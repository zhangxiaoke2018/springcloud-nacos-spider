package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.ChildrenBook;
import com.jinguduo.spider.service.ChildrenBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/12/4
 */
@RestController
@RequestMapping("/children_book")
public class ChildrenBookController {

    @Autowired
    private ChildrenBookService service;

    @PostMapping
    public ChildrenBook save(@RequestBody ChildrenBook cb) {
        return service.saveOrUpdate(cb);
    }
}
