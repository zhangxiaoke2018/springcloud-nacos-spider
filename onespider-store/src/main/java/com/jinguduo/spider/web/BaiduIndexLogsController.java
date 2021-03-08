package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.BaiduIndexLogs;
import com.jinguduo.spider.service.BaiduIndexLogsService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 05/12/2016 7:37 PM
 */
@RestController
@RequestMapping("/baidu_index_logs")
public class BaiduIndexLogsController {

    @Autowired
    private BaiduIndexLogsService baiduIndexLogsService;

    @RequestMapping(value = "",method = RequestMethod.POST)
    public void save(@RequestBody BaiduIndexLogs baiduIndexLogs){
        baiduIndexLogsService.save(baiduIndexLogs);
    }


}
