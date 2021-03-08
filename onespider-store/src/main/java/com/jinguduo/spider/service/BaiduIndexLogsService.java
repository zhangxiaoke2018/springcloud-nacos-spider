package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.BaiduIndexLogs;
import com.jinguduo.spider.db.repo.BaiduIndexLogsRepo;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 05/12/2016 7:34 PM
 */
@Service
@CommonsLog
public class BaiduIndexLogsService {

    @Autowired
    private BaiduIndexLogsRepo baiduIndexLogsRepo;

    public void save(BaiduIndexLogs baiduIndexLogs){
        baiduIndexLogsRepo.save(baiduIndexLogs);
    }



}
