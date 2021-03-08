package com.jinguduo.spider.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.ExponentLog;
import com.jinguduo.spider.db.repo.ExponentLogRepo;
import com.jinguduo.spider.service.ExponentLogService;

@Service
public class ExponentLogServiceImpl implements ExponentLogService {

    @Autowired
    private ExponentLogRepo exponentLogRepo;

    @Override
    public ExponentLog insertOrUpdate(ExponentLog exponentLog) {

        if(exponentLog == null || exponentLog.getExponentNum() <= 0L) {
            return null;
        }

        ExponentLog e = exponentLogRepo.findByCodeAndExponentDate(exponentLog.getCode(),exponentLog.getExponentDate());
        if(e!=null){
            if(e.getExponentNum() <= 0L){
                e.setExponentNum(exponentLog.getExponentNum());
                exponentLogRepo.save(e);
                return e;
            }
        }else{
            return exponentLogRepo.save(exponentLog);
        }
        return null;
    }
}
