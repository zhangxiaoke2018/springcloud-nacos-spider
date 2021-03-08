package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicDmzj;
import com.jinguduo.spider.db.repo.ComicDmzjRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/4
 * Time:18:21
 */
@Service
@Slf4j
public class ComicDmzjService {
    @Autowired
    private ComicDmzjRepo comicDmzjRepo;

    public ComicDmzj insertOrUpdate(ComicDmzj comicDmzj) {
        if (null == comicDmzj
                ||null == comicDmzj.getDay()
                || StringUtils.isBlank(comicDmzj.getCode())) {
            return null;
        }
        boolean isSkip = skipByCode(comicDmzj.getCode());
        if (isSkip) {
            return comicDmzj;
        }

        ComicDmzj cu = comicDmzjRepo.findByCodeAndDay(comicDmzj.getCode(),comicDmzj.getDay());
        //如果有该数据,插入id
        if (null != cu) {
            try {
                DbEntityHelper.merge(cu, comicDmzj);
                return comicDmzjRepo.save(cu);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            return comicDmzjRepo.save(comicDmzj);
        }
        return null;
    }


}
