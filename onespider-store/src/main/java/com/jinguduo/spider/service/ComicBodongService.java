package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicBodong;
import com.jinguduo.spider.db.repo.ComicBodongRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * Created by lc on 2018/9/10
 */
@Service
@Slf4j
public class ComicBodongService {
    @Autowired
    private ComicBodongRepo repo;

    public ComicBodong insertOrUpdate(ComicBodong bodong) {
        if (null == bodong
                || null == bodong.getDay()
                || StringUtils.isBlank(bodong.getCode())) {
            return null;
        }
        boolean isSkip = skipByCode(bodong.getCode());
        if (isSkip) {
            return bodong;
        }

        ComicBodong old = repo.findByCodeAndDay(bodong.getCode(), bodong.getDay());
        //如果有该数据,插入id
        if (null != old) {
            if (bodong.getReadCount() != null) {
                old.setReadCount(bodong.getReadCount());
            }
            if (bodong.getReadPicCount() != null) {
                old.setReadPicCount(bodong.getReadPicCount());
            }
            if (bodong.getCollectCount() != null) {
                old.setCollectCount(bodong.getCollectCount());
            }
            if (bodong.getCommentCount() != null) {
                old.setCommentCount(bodong.getCommentCount());
            }
            repo.save(old);
        } else {
            return repo.save(bodong);
        }
        return null;
    }
}
