package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.ToutiaoNewLogs;
import com.jinguduo.spider.service.NewsToutiaoLogsService;

/**
 * Created by lc on 2017/5/15.
 */
@RestController
@RequestMapping("/news_toutiao_logs")
public class NewsToutiaoLogsController {

    @Autowired
    private NewsToutiaoLogsService toutiaoLogsService;

    @PostMapping
    public ToutiaoNewLogs insertOrUpdate(@RequestBody ToutiaoNewLogs logs) {
        return toutiaoLogsService.save(logs);
    }

}
