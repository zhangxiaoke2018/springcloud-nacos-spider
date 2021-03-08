package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WechatBusiness;
import com.jinguduo.spider.db.repo.WechatBusinessRepo;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 14/03/2017 11:04 AM
 */
@Service
@CommonsLog
public class WechatBusinessService {

    @Autowired
    private WechatBusinessRepo wechatBusinessRepo;


    public List<WechatBusiness> findAll(){
        return wechatBusinessRepo.findAll();
    }

    public List<WechatBusiness> findAllGreatest(){
        return wechatBusinessRepo.findByGreatest(Boolean.TRUE);
    }


    /**
     * 以w_id为准,唯一
     * @param wechat
     * @return
     */
    public WechatBusiness insertOrUpdate(WechatBusiness wechat) {

        WechatBusiness majorWechat = wechatBusinessRepo.findByWechatId(wechat.getWechatId());

        if(majorWechat != null){//存在进行更新
            majorWechat.setName(wechat.getName());
            return wechatBusinessRepo.save(majorWechat);
        }else {
            return wechatBusinessRepo.save(wechat);
        }
    }
}
