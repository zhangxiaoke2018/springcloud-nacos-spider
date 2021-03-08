package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WeiboAttribute;
import com.jinguduo.spider.db.repo.WeiboAttributeRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 20/04/2017 14:30
 */
@Service
@CommonsLog
public class WeiboAttributeService {

    @Autowired
    private WeiboAttributeRepo weiboAttributeRepo;

    public WeiboAttribute insertOrUpdate(WeiboAttribute weiboAttribute) {

        if (StringUtils.isBlank(weiboAttribute.getKeyword())) {
            return null;
        }
        WeiboAttribute wa = weiboAttributeRepo.save(weiboAttribute);
        return wa;
    }

}
