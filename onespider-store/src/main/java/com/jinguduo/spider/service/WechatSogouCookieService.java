package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WechatSogouCookie;
import com.jinguduo.spider.db.repo.WechatSogouCookieRepo;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/4/30
 */
@Service
public class WechatSogouCookieService {
    @Autowired
    private WechatSogouCookieRepo repo;


    public Iterable<WechatSogouCookie> findAll() {
        Iterable<WechatSogouCookie> all = repo.findAll();
        if (all != null) {
            return all;
        }
        return Lists.emptyList();

    }
}
