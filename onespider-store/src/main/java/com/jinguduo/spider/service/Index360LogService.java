package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Index360Logs;
import com.jinguduo.spider.db.repo.Index360LogRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2017/5/5.
 */
@Service
public class Index360LogService {


    private final Logger logger = LoggerFactory.getLogger(Index360LogService.class);
    @Autowired
    private Index360LogRepo index360LogRepo;

    public Index360Logs save(Index360Logs logs) {

        if (null == logs) {
            logger.error("***save Index360Logs error ,because Index360Logs is null ***");
            return null;
        }
        if (logs.getIndexCount().equals(0)) {
            return logs;
        }
        Index360Logs logsByCodeAndDay = index360LogRepo.findByCodeAndIndexDay(logs.getCode(), logs.getIndexDay());
        //如果有该数据,插入id
        if (null != logsByCodeAndDay) {
            if (logs.getIndexCount().equals(0) || logs.getIndexCount().equals(logsByCodeAndDay.getIndexCount())) {
                return logsByCodeAndDay;
            }
            logsByCodeAndDay.setIndexCount(logs.getIndexCount());
            return index360LogRepo.save(logsByCodeAndDay);
        } else {
            Index360Logs save = index360LogRepo.save(logs);
            return save;
        }
    }

}
