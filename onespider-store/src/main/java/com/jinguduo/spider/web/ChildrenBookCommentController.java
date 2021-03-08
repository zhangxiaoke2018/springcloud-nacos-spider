package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookComment;
import com.jinguduo.spider.service.ChildrenBookCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/12/4
 */
@RestController
@RequestMapping("/children_book_comment")
public class ChildrenBookCommentController {
    @Autowired
    private ChildrenBookCommentService service;

    @PostMapping
    public ChildrenBookComment save(@RequestBody ChildrenBookComment cbc) {
        return service.saveOrUpdate(cbc);
    }
}
