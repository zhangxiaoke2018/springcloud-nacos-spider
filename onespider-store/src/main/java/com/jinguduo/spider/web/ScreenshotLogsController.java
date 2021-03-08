package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ScreenshotLogs;
import com.jinguduo.spider.service.ScreenshotLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 19/05/2017 17:27
 */
@RestController
@RequestMapping("/screenshot_logs")
public class ScreenshotLogsController {

    @Autowired
    private ScreenshotLogsService screenshotLogsService;

    @PostMapping("/")
    public Object insertOrUpdate(@RequestBody ScreenshotLogs screenshotLogs){

        screenshotLogsService.insertOrUpdate(screenshotLogs);

        return "SUCCESS";
    }




}
