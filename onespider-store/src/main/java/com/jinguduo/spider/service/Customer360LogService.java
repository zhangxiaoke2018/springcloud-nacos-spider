package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.Customer360Logs;
import com.jinguduo.spider.db.repo.Customer360LogRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/11
 * Time:10:59
 */
@Service
@Slf4j
public class Customer360LogService {

    @Autowired
    private Customer360LogRepo customer360LogRepo;

    public Customer360Logs save(Customer360Logs logs) {

        if (null == logs) {
            log.error("***save Customer360Logs error ,because Customer360Logs is null ***");
            return null;
        }
        //统一时间
        logs.setDay(DateUtil.getDayStartTime(logs.getDay()));
        Customer360Logs logsByCodeAndProAndDay = customer360LogRepo.findByCodeAndProvinceAndDay(logs.getCode(), logs.getProvince(),logs.getDay());
        //如果有该数据,插入id
        if (null != logsByCodeAndProAndDay) {
            logs.setId(logsByCodeAndProAndDay.getId());
            return customer360LogRepo.save(logs);
        } else {
            Customer360Logs save = customer360LogRepo.save(logs);
            return save;
        }
    }
}
