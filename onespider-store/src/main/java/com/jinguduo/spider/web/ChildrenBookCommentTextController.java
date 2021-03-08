package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookCommentText;
import com.jinguduo.spider.service.ChildrenBookCommentTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/12/4
 */
@RestController
@RequestMapping("/children_book_comment_text")
public class ChildrenBookCommentTextController {
    @Autowired
    private ChildrenBookCommentTextService service;

    @PostMapping
    public ChildrenBookCommentText save(@RequestBody ChildrenBookCommentText text) {
        return service.saveOrUpdate(text);
    }
}
