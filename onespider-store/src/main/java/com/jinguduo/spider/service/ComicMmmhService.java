package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicMmmh;
import com.jinguduo.spider.db.repo.ComicMmmhRepo;
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
public class ComicMmmhService {
    @Autowired
    private ComicMmmhRepo comicMmmhRepo;

    public ComicMmmh insertOrUpdate(ComicMmmh comicMmmh) {
        if (null == comicMmmh
                || null == comicMmmh.getDay()
                || StringUtils.isBlank(comicMmmh.getCode())) {
            return null;
        }
        boolean isSkip = skipByCode(comicMmmh.getCode());
        if (isSkip) {
            return comicMmmh;
        }


        ComicMmmh old = comicMmmhRepo.findByCodeAndDay(comicMmmh.getCode(), comicMmmh.getDay());
        //如果有该数据,插入id
        if (null != old) {
            if (old.getCommentNum() == null) {
                old.setCommentNum(comicMmmh.getCommentNum());
            }
            if (old.getLikesNum() == null) {
                old.setLikesNum(comicMmmh.getLikesNum());
            }
            if (old.getReadsNum() == null) {
                old.setReadsNum(comicMmmh.getReadsNum());
            }
        } else {
            return comicMmmhRepo.save(comicMmmh);
        }
        return old;
    }


}
