package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.BaiduNewsLog;
import com.jinguduo.spider.data.table.TiebaLog;
import com.jinguduo.spider.db.repo.BaiduNewsLogRepo;
import com.jinguduo.spider.db.repo.TiebaLogRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 17:17
 */
@Service
@CommonsLog
public class BaiduNewsLogService {

    @Autowired
    private BaiduNewsLogRepo baiduNewsLogRepo;

    public BaiduNewsLog insertOrUpdate(BaiduNewsLog log){

        if(log == null){
            return null;
        }
        return baiduNewsLogRepo.save(log);
    }


}
