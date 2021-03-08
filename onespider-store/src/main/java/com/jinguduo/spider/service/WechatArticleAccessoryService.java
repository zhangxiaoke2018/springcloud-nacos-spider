package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WechatArticleAccessory;
import com.jinguduo.spider.db.repo.WechatArticleAccessoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @DATE 2018/10/12 3:58 PM
 */
@Service
public class WechatArticleAccessoryService {

    @Autowired
    private WechatArticleAccessoryRepo wechatArticleAccessoryRepo;


    public WechatArticleAccessory insert(WechatArticleAccessory waa) {
        assert waa != null;
        assert waa.getId() == null;

        return wechatArticleAccessoryRepo.save(waa);
    }
}
