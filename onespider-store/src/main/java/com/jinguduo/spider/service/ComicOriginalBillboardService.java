package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicOriginalBillboard;
import com.jinguduo.spider.db.repo.ComicOriginalBillboardRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/5/29
 */
@Service
public class ComicOriginalBillboardService {

    @Autowired
    private ComicOriginalBillboardRepo billboardRepo;


    public ComicOriginalBillboard insertOrUpdate(ComicOriginalBillboard billboard) {
        if (null == billboard
                || null == billboard.getDay()
                || null == billboard.getPlatformId()
                || StringUtils.isEmpty(billboard.getBillboardType())
                || null == billboard.getRank()
                || StringUtils.isEmpty(billboard.getCode())) {
            return null;
        }
        ComicOriginalBillboard old = billboardRepo.findByDayAndPlatformIdAndBillboardTypeAndRank(billboard.getDay(),
                billboard.getPlatformId(),
                billboard.getBillboardType(),
                billboard.getRank());

        if (null == old) {
            return billboardRepo.save(billboard);
        } else {
            old.setCode(billboard.getCode());
            old.setName(billboard.getName());
            old.setBillboardUpdateTime(billboard.getBillboardUpdateTime());
            return billboardRepo.save(old);
        }

    }
}
