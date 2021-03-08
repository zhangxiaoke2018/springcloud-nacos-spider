package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.JianshuBookLogs;
import com.jinguduo.spider.service.JianshuBookLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2020/1/17
 */
@RestController
@RequestMapping("/jianshu_book_logs")
public class JianshuBookLogsController {

    @Autowired
    JianshuBookLogsService service;

    @PostMapping
    public JianshuBookLogs save(@RequestBody JianshuBookLogs logs){
        return service.saveOrUpdate(logs);
    }
}
