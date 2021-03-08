package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicKanmanhua;
import com.jinguduo.spider.db.repo.ComicKanmanhuaRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2018/1/26
 * Time:13:49
 */

@Service
@Slf4j
public class ComicKanmanhuaService {
    @Autowired
    private ComicKanmanhuaRepo comicKanmanhuaRepo;


    public ComicKanmanhua insertOrUpdate(ComicKanmanhua kan) {
        if (null == kan) {
            return null;
        }
        boolean isSkip = skipByCode(kan.getCode());
        if (isSkip) {
            return kan;
        }


        ComicKanmanhua cu = comicKanmanhuaRepo.findByCodeAndDay(kan.getCode(), kan.getDay());
        //如果有该数据,插入id
        if (null != cu) {
            try {
                DbEntityHelper.merge(cu, kan);
                return comicKanmanhuaRepo.save(cu);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            return comicKanmanhuaRepo.save(kan);
        }
        return null;
    }

}
