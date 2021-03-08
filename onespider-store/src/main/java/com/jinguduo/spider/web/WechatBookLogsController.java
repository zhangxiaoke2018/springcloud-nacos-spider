package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.WechatBookLogs;
import com.jinguduo.spider.service.WechatBookLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2020/1/17
 */
@RestController
@RequestMapping("/wechat_book_logs")
public class WechatBookLogsController {

    @Autowired
    WechatBookLogsService service;

    @PostMapping
    public WechatBookLogs save(@RequestBody WechatBookLogs logs) {
        return service.saveOrUpdate(logs);
    }
}
