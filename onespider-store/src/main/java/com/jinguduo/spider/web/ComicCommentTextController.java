package com.jinguduo.spider.web;


import com.jinguduo.spider.data.table.ComicCommentText;
import com.jinguduo.spider.service.ComicCommentTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comic_comment_text")
public class ComicCommentTextController {

    @Autowired
    private ComicCommentTextService comicCommentTextService;

    @PostMapping("/insert_comment_text")
    public ComicCommentText save(@RequestBody ComicCommentText comicCommentText) {
        return comicCommentTextService.insertOrUpdate(comicCommentText);
    }
}
