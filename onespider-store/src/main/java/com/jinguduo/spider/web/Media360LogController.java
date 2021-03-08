package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Media360Logs;
import com.jinguduo.spider.service.Media360LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2017/5/10.
 */

@RestController
@RequestMapping("/media_360_logs")
public class Media360LogController {

    @Autowired
    private Media360LogsService media360LogsService;

    @PostMapping
    public Media360Logs insertOrUpdate(@RequestBody Media360Logs logs) {
        return media360LogsService.save(logs);

    }
}
