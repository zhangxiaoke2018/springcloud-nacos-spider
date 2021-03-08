package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.TiebaLog;
import com.jinguduo.spider.service.TiebaLogService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 15:27
 */
@RestController
@RequestMapping("/tieba_log")
public class TiebaLogsController {

    @Autowired
    private TiebaLogService tiebaLogService;

    @PostMapping
    public TiebaLog insertOrUpdate(@RequestBody TiebaLog tiebaLog){

        return tiebaLogService.insertOrUpdate(tiebaLog);

    }





}
