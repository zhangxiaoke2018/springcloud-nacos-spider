package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.CrabSettings;
import com.jinguduo.spider.db.repo.CrabSettingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/05/2017 15:52
 */
@Service
public class CrabSettingsService {

    @Autowired
    private CrabSettingsRepo crabSettingsRepo;

    //supervisor 调用
    public List<CrabSettings> find(Long time){
        Assert.notNull(time);
        return crabSettingsRepo.findByUpdatedAtGreaterThan(new Timestamp(time));
    }


}
