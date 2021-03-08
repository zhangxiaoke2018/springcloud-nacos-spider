package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.SpiderSetting;
import com.jinguduo.spider.db.repo.SpiderSettingRepo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/28 下午6:55
 */
@Service
public class SpiderSettingService {

    @Autowired
    SpiderSettingRepo settingRepo;

    
    /**
     * 获得SpiderSetting对象，如果没有就创建一个并保存返回
     * @param domain
     * @return
     */
    public SpiderSetting findOne(String domain){

        SpiderSetting setting = settingRepo.findByDomain(domain);
        if (setting == null) {
            setting = new SpiderSetting();
            setting.setDomain(domain);
            settingRepo.saveAndFlush(setting);
        }
        return setting;
    }

    public List<SpiderSetting> findAll(){

        List<SpiderSetting> settings = settingRepo.findAll();

        return settings;
    }

    public SpiderSetting insertOrUpdate(SpiderSetting setting){

        assert setting == null;

        if(setting.getId() != null){
            SpiderSetting one = settingRepo.findOne(setting.getId());
            BeanUtils.copyProperties(setting,one);
            return settingRepo.save(one);
        }else {
            return settingRepo.save(setting);
        }
    }
    
    public SpiderSetting findByDomain(String domain){
        return settingRepo.findByDomain(domain);
    }
    
    public Page<SpiderSetting> findSettingPage(int page,int size){
        Pageable pageable = new PageRequest(page, size);
        return settingRepo.findAll(pageable);
    }

    public SpiderSetting findOneById(Integer id){
        return settingRepo.findOne(id);
    }
}
