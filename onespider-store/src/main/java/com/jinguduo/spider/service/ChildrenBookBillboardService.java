package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookBillboard;
import com.jinguduo.spider.db.repo.ChildrenBookBillboardRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/12/4
 */
@Service
public class ChildrenBookBillboardService {
    @Autowired
    ChildrenBookBillboardRepo repo;

    public ChildrenBookBillboard saveOrUpdate(ChildrenBookBillboard cbb) {

        ChildrenBookBillboard old = repo.findByDayAndPlatformIdAndTypeAndRank(cbb.getDay(), cbb.getPlatformId(), cbb.getType(), cbb.getRank());

        if (null == old) {
            return repo.save(cbb);
        } else {
            old.setCode(cbb.getCode());
            return repo.save(old);
        }

    }
}
