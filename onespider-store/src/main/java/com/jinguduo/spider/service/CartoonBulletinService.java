package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.CartoonBulletin;
import com.jinguduo.spider.db.repo.CartoonBulletinRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2020/4/9
 */
@Service
public class CartoonBulletinService {
    @Autowired
    private CartoonBulletinRepo repo;

    public CartoonBulletin saveOrUpdate(CartoonBulletin cb) {

        CartoonBulletin old = repo.findFirstByUrl(cb.getUrl());
        //只保存新的，不更新旧的
        if (old == null) {
            return repo.save(cb);
        }
        return old;
    }
}
