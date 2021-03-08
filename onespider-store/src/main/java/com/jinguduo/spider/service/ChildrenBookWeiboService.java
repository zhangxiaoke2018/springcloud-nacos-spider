package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookWeibo;
import com.jinguduo.spider.db.repo.ChildrenBookWeiboRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2020/2/17
 */
@Service
public class ChildrenBookWeiboService {
    @Autowired
    private ChildrenBookWeiboRepo repo;

    public ChildrenBookWeibo saveOrUpdate(ChildrenBookWeibo cb) {
        ChildrenBookWeibo old = repo.findFirstByMidAndCodeAndPlatformId(cb.getMid(), cb.getCode(), cb.getPlatformId());

        if (null == old) {
            return repo.save(cb);
        } else {
            old.setZhuan(cb.getZhuan());
            old.setPing(cb.getPing());
            old.setZan(cb.getZan());
            return repo.save(old);
        }

    }
}
