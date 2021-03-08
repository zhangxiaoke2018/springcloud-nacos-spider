package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicDmmh;
import com.jinguduo.spider.data.table.ComicDmmhComment;
import com.jinguduo.spider.service.ComicDmmhCommentService;
import com.jinguduo.spider.service.ComicDmmhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 01/08/2017 09:32
 */
@RestController
@RequestMapping("/comic_dmmh_comment")
public class ComicDmmhCommentController {

    @Autowired
    private ComicDmmhCommentService comicDmmhCommentService;

    @PostMapping
    public ComicDmmhComment save(@RequestBody ComicDmmhComment comicDmmh){

        ComicDmmhComment c = comicDmmhCommentService.insertOrUpdate(comicDmmh);

        return c;
    }


}
