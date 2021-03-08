package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.Customer360Logs;
import com.jinguduo.spider.service.Customer360LogService;

/**
 * Created by lc on 2017/5/5.
 */
@RestController
@RequestMapping("/customer_360_logs")
public class Customer360LogsController {

    @Autowired
    private Customer360LogService customer360LogService;

    @PostMapping
    public Customer360Logs insertOrUpdate(@RequestBody Customer360Logs logs) {
        return customer360LogService.save(logs);

    }
}
