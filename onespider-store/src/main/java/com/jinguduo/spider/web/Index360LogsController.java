package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Index360Logs;
import com.jinguduo.spider.service.Index360LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2017/5/5.
 */
@RestController
@RequestMapping("/index_360_logs")
public class Index360LogsController {

    @Autowired
    private Index360LogService index360LogService;

    @PostMapping
    public Index360Logs insertOrUpdate(@RequestBody Index360Logs logs) {
        return index360LogService.save(logs);

    }
}
