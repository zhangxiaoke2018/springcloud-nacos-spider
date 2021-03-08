package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicBanner;
import com.jinguduo.spider.db.repo.ComicBannerRepo;
import com.jinguduo.spider.db.repo.ComicRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * Created by lc on 2019/8/12
 */
@Service
public class ComicBannerService {
    @Autowired
    private ComicRepo comicRepo;

    @Autowired
    private ComicBannerRepo bannerRepo;

    public ComicBanner insertOrUpdate(ComicBanner banner) {
        if (null == banner.getCode() || null == banner.getPlatformId() || null == banner.getDay()) {
            return banner;
        }
        boolean isSkip = skipByCode(banner.getCode());
        if (isSkip) {
            return banner;
        }
        ComicBanner old = bannerRepo.findByCodeAndPlatformIdAndDay(banner.getCode(), banner.getPlatformId(), banner.getDay());

        if (null != old) return old;
        //拿不到name 的情况
        if (StringUtils.isEmpty(banner.getName())) {
            Comic comic = comicRepo.findByCode(banner.getCode());
            if (null == comic) {
                return banner;
            }
            banner.setName(comic.getName());
        }
        bannerRepo.save(banner);

        return null;
    }
}
