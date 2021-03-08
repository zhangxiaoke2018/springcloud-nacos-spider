package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.service.AutoFindLogsService;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年9月7日 下午4:59:19
 *
 */
@RestController
public class AutoFindLogsController {

    @Autowired
    private AutoFindLogsService autoFindLogsService;

    @RequestMapping(value = "/autofindlogs", method = RequestMethod.POST)
    public Object saveAutoFindLogs(@RequestBody List<AutoFindLogs> logs) {
        for (AutoFindLogs log : logs) {
            autoFindLogsService.save(log);
        }
        return "OK";
    }

}
