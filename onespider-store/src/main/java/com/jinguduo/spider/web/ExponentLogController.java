package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ExponentLog;
import com.jinguduo.spider.service.ExponentLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExponentLogController {

    @Autowired
    private ExponentLogService exponentLogService;

    @RequestMapping(path = "/insert_exponent_log", method = RequestMethod.POST)
    public boolean saveCommentLog(@RequestBody ExponentLog exponentLog) {

        return null != exponentLogService.insertOrUpdate(exponentLog) ? true : false;
    }

}
