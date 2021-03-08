package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.StockBulletin;
import com.jinguduo.spider.db.repo.StockBulletinRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/4/9
 */
@Service
public class StockBulletinService {

    @Autowired
    StockBulletinRepo repo;

    public StockBulletin save(StockBulletin sb) {
        StockBulletin old = repo.findByCodeAndCompanyCode(sb.getCode(), sb.getCompanyCode());

        //update
        if (null != old) {
           return old;
        }
        //insert
        return repo.save(sb);

    }
}
