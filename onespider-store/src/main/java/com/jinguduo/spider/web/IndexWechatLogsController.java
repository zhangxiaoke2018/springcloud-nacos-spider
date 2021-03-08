package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.IndexWechatLogs;
import com.jinguduo.spider.service.IndexWechatLogService;

/**
 * Created by lc on 2017/5/5.
 */
@RestController
@RequestMapping("/index_wechat_logs")
public class IndexWechatLogsController {

    @Autowired
    private IndexWechatLogService indexWechatLogService;

    @PostMapping
    public IndexWechatLogs insertOrUpdate(@RequestBody IndexWechatLogs logs) {
        return indexWechatLogService.save(logs);

    }
}
