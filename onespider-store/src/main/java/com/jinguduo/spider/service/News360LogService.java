package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.BaiduNewsLog;
import com.jinguduo.spider.data.table.News360Log;
import com.jinguduo.spider.db.repo.BaiduNewsLogRepo;
import com.jinguduo.spider.db.repo.News360LogRepo;
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
public class News360LogService {

    @Autowired
    private News360LogRepo news360LogRepo;

    public News360Log insertOrUpdate(News360Log log){

        if(log == null){
            return null;
        }
        return news360LogRepo.save(log);
    }


}
