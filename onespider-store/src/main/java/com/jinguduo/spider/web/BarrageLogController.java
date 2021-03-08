package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.service.BarrageLogService;

/**
 * Created by csonezp on 2016/10/28.
 */
@RestController
public class BarrageLogController {
    @Autowired
    BarrageLogService barrageLogService;

    @RequestMapping(path = "/insert_barrage_log", method = RequestMethod.POST)
    public boolean saveBarrageLog(@RequestBody BarrageLog barrageLog) {

        return null != barrageLogService.insert(barrageLog) ? true : false;
    }

}
