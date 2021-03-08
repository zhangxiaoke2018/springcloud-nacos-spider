package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.db.repo.BarrageLogRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by csonezp on 2016/10/28.
 */
@Service
public class BarrageLogService {
    @Autowired
    BarrageLogRepo barrageLogRepo;

    public BarrageLog insert(BarrageLog barrageLog) {

        return barrageLogRepo.save(barrageLog);
    }
}
