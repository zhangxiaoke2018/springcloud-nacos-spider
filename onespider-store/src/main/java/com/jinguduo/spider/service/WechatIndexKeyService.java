package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WechatIndexKey;
import com.jinguduo.spider.data.table.WechatParam;
import com.jinguduo.spider.db.repo.WechatIndexKeyRepo;
import com.jinguduo.spider.db.repo.WechatParamRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @DATE 2018/10/11 11:12 AM
 */
@Service
public class WechatIndexKeyService {

    @Autowired
    private WechatIndexKeyRepo wechatIndexKeyRepo;


    public WechatIndexKey save(String openId, String searchKey){

        WechatIndexKey wechatIndexKey = wechatIndexKeyRepo.findByOpenId(openId);

        if(wechatIndexKey == null){
            WechatIndexKey wik = new WechatIndexKey(openId, searchKey);
            wik.setKeyUpdatedTime(new Date());
            return wechatIndexKeyRepo.save(wik);
        } else {
            wechatIndexKey.setSearchKey(searchKey);
            wechatIndexKey.setKeyUpdatedTime(new Date());
            return wechatIndexKeyRepo.save(wechatIndexKey);
        }
    }
}
