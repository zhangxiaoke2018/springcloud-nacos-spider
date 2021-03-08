package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WeiboProvinceCompare;
import com.jinguduo.spider.db.repo.WeiboProvinceCompareRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 20/04/2017 14:30
 */
@Service
@CommonsLog
public class WeiboProvinceCompareService {

    @Autowired
    private WeiboProvinceCompareRepo weiboProvinceCompareRepo;

    public void insertOrUpdate(WeiboProvinceCompare weiboProvinceCompare) throws IllegalArgumentException, IllegalAccessException {

        if (StringUtils.isBlank(weiboProvinceCompare.getKeyword())) {
            return;
        }

        WeiboProvinceCompare newWeiboProvinceCompare = new WeiboProvinceCompare();
        BeanUtils.copyProperties(weiboProvinceCompare, newWeiboProvinceCompare);
        weiboProvinceCompare.fill("zone");
        newWeiboProvinceCompare.fill("user");
        if(weiboProvinceCompare.getTotal() != 0L){
            weiboProvinceCompareRepo.save(weiboProvinceCompare);
        }
        if(newWeiboProvinceCompare.getTotal() != 0L){
            weiboProvinceCompareRepo.save(newWeiboProvinceCompare);
        }

    }

}
