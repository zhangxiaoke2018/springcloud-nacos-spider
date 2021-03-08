package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.db.repo.VipEpisodeRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/3
 * Time:16:21
 */
@Service
@Slf4j
public class VipEpisodeService {
    @Autowired
    private VipEpisodeRepo vipRepo;

    public VipEpisode save(VipEpisode vip) {

        if(vip.getPlatformId() == null) {
            return null;
        }

        if (null == vip || StringUtils.isBlank(vip.getCode())) {
            log.error("***save Index360Logs error ,because Index360Logs is null ***");
            return null;
        }
        //适配结束时间为空
        vip.setVipEndTime(null == vip.getVipEndTime() ? new Date() : vip.getVipEndTime());

        VipEpisode vipData = vipRepo.findByCodeAndPlatformId(vip.getCode(), vip.getPlatformId());
        //如果有该数据,插入id
        if (null != vipData) {
            vipData.setVipEndTime(vip.getVipEndTime());
            return vipRepo.save(vipData);
        } else {
            return vipRepo.save(vip);
        }
    }
}
