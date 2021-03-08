package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.service.CommentLogService;

@RestController
public class CommentLogController {

    @Autowired
    private CommentLogService commentLogService;

    @RequestMapping(path = "/insert_comment_log", method = RequestMethod.POST)
    public boolean saveCommentLog(@RequestBody CommentLog commentLog) {
        return null != commentLogService.insert(commentLog) ? true : false;
    }

    @RequestMapping(path = "/insert_comment_logs", method = RequestMethod.POST)
    public int saveCommentLog(@RequestBody List<CommentLog> commentLogs) {

        return commentLogService.insertListReturnNum(commentLogs);
    }

}
