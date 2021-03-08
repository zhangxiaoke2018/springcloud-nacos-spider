package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.FictionOriginalBillboard;
import com.jinguduo.spider.db.repo.FictionOriginalBillboardRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/6/27
 */
@Service
public class FictionOriginalBillboardService {
    @Autowired
    FictionOriginalBillboardRepo repo;

    public FictionOriginalBillboard saveOrUpdate(FictionOriginalBillboard billboard) {
        FictionOriginalBillboard old = repo.findByPlatformIdAndTypeAndDayAndRank(billboard.getPlatformId(), billboard.getType(), billboard.getDay(), billboard.getRank());

        if (null == old) {
            return repo.save(billboard);
        } else {
            old.setCode(billboard.getCode());
            old.setBillboardUpdateTime(billboard.getBillboardUpdateTime());
            return repo.save(old);
        }


    }
}
