package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WeiboIndexHourLog;
import com.jinguduo.spider.db.repo.WeiboIndexHourLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeiboIndexHourLogService {

    @Autowired
    private WeiboIndexHourLogRepo weiboIndexHourLogRepo;

    public WeiboIndexHourLog insertOrUpdate(WeiboIndexHourLog indexHourLog) {

        if(indexHourLog == null || indexHourLog.getIndexCount() <= 0L) {
            return null;
        }

        WeiboIndexHourLog e = weiboIndexHourLogRepo.findByCodeAndHour(indexHourLog.getCode(),indexHourLog.getHour());
        if(e != null){
            if(e.getIndexCount() <= 0L){
                e.setIndexCount(indexHourLog.getIndexCount());
                weiboIndexHourLogRepo.save(e);
                return e;
            }
        }else{
            return weiboIndexHourLogRepo.save(indexHourLog);
        }
        return null;
    }
}
