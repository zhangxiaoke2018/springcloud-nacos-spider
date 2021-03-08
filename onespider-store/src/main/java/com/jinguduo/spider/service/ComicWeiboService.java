package com.jinguduo.spider.service;


import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicWeibo;
import com.jinguduo.spider.db.repo.ComicWeiboRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 *
 *   author : xk
 * */

@Service
@Slf4j
public class ComicWeiboService {
    @Autowired
    private ComicWeiboRepo comicWeiboRepo;

    public ComicWeibo insertOrUpdate(ComicWeibo cw) {
        if (null == cw) {
            return null;
        }
        boolean isSkip = skipByCode(cw.getCode());
        if (isSkip) {
            return cw;
        }
        ComicWeibo old = comicWeiboRepo.findByCodeAndDay(cw.getCode(), cw.getDay());
        //如果有该数据,插入id
        if (null != old) {
            try {
                DbEntityHelper.merge(old, cw);
                return comicWeiboRepo.save(old);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            return comicWeiboRepo.save(cw);
        }
        return null;
    }

}
