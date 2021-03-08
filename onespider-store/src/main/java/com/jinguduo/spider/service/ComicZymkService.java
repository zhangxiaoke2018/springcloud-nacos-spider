package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicZymk;
import com.jinguduo.spider.db.repo.ComicZymkRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/10/20
 * Time:11:40
 */
@Service
@Slf4j
public class ComicZymkService {
    @Autowired
    private ComicZymkRepo comicZymkRepo;

    public ComicZymk insertOrUpdate(ComicZymk zymk) {
        if (null == zymk
                ||null == zymk.getDay()
                || StringUtils.isBlank(zymk.getCode())) {
            return null;
        }

        boolean isSkip = skipByCode(zymk.getCode());
        if (isSkip) {
            return zymk;
        }
        ComicZymk cu = comicZymkRepo.findByCodeAndDay(zymk.getCode(),zymk.getDay());
        //如果有该数据,插入id
        //如果有该数据,插入id
        if (null != cu) {
            try {
                DbEntityHelper.merge(cu, zymk);
                return comicZymkRepo.save(cu);
            } catch (Exception e) {
                log.error("save ComicZymk error ->",e);
            }
        } else {
            return comicZymkRepo.save(zymk);
        }
        return null;
    }
}
