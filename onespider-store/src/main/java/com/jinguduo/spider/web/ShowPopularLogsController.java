package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ShowPopularLogs;
import com.jinguduo.spider.service.ShowPopularLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by lc on 2018/9/3
 */
@RestController
@RequestMapping("/show_popular_logs")
public class ShowPopularLogsController {
    @Autowired
    ShowPopularLogsService service;

    @PostMapping("/save")
    public ShowPopularLogs insert(@RequestBody ShowPopularLogs logs) {
        return service.save(logs);

    }
}
