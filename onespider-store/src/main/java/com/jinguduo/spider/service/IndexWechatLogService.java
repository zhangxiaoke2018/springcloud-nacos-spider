package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.IndexWechatLogs;
import com.jinguduo.spider.db.repo.IndexWechatLogRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2017/5/5.
 */
@Service
public class IndexWechatLogService {


    private final Logger logger = LoggerFactory.getLogger(IndexWechatLogService.class);
    @Autowired
    private IndexWechatLogRepo indexWechatLogRepo;

    public IndexWechatLogs save(IndexWechatLogs logs) {

        if (null == logs) {
            logger.error("***save IndexWechatLogs error ,because IndexWechatLogs is null ***");
            return null;
        }
        IndexWechatLogs logsByCodeAndDay = indexWechatLogRepo.findByCodeAndIndexDay(logs.getCode(), logs.getIndexDay());
        //如果有该数据,插入id
        if (null != logsByCodeAndDay) {
            if (logs.getIndexDay().equals(0)){
                return logsByCodeAndDay;
            }
            logsByCodeAndDay.setIndexCount(logs.getIndexCount());
            return indexWechatLogRepo.save(logsByCodeAndDay);
        } else {
            return  indexWechatLogRepo.save(logs);
        }
        //统计
    }

}
