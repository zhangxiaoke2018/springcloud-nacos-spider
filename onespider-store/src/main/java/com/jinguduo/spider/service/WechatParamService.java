package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WechatParam;
import com.jinguduo.spider.db.repo.WechatParamRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @DATE 2018/10/11 11:12 AM
 */
@Service
public class WechatParamService {

    @Autowired
    private WechatParamRepo wechatParamRepo;

    public String findValue(String key){
        WechatParam wechatParam = wechatParamRepo.findByParamKey(key);

        if(wechatParam != null) {
            return wechatParam.getParamValue();
        }else {
            return "";
        }
    }

    public String update(String key, String value){

        WechatParam wechatParam = wechatParamRepo.findByParamKey(key);

        if(wechatParam == null) {
            return "FALSE";
        }else {

            wechatParam.setParamValue(value);
            wechatParamRepo.save(wechatParam);

            return "SUCCESS";
        }

    }


}
