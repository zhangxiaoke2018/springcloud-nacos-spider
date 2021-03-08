package com.jinguduo.spider.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.Media360Logs;
import com.jinguduo.spider.db.repo.Media360LogRepo;

/**
 * Created by lc on 2017/5/10.
 */
@Service
public class Media360LogsService {
    private final Logger logger = LoggerFactory.getLogger(Index360LogService.class);
    @Autowired
    private Media360LogRepo media360LogRepo;

    public Media360Logs save(Media360Logs logs) {

        if (null == logs) {
            logger.error("***save Media360Logs error ,because Media360Logs is null ***");
            return null;
        }
        if (logs.getMediaCount().equals(0)) {
            return logs;
        }
        Media360Logs logsByCodeAndDay = media360LogRepo.findByCodeAndMediaDay(logs.getCode(), logs.getMediaDay());
        //如果有该数据,插入id
        if (null != logsByCodeAndDay) {
            if (logs.getMediaCount().equals(0)){
                return logsByCodeAndDay;
            }
            logsByCodeAndDay.setMediaCount(logs.getMediaCount());
            Media360Logs save = media360LogRepo.save(logsByCodeAndDay);
            return save;
        } else {
            return  media360LogRepo.save(logs);
        }
    }

}
