package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookWeibo;
import com.jinguduo.spider.service.ChildrenBookWeiboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2020/2/17
 */
@RestController
@RequestMapping("/children_book_weibo")
public class ChildrenBookWeiboController {

    @Autowired
    private ChildrenBookWeiboService service;

    @PostMapping
    public ChildrenBookWeibo save(@RequestBody ChildrenBookWeibo cb) {
        return service.saveOrUpdate(cb);
    }
}
