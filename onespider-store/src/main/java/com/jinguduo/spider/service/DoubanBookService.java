package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.DoubanBook;
import com.jinguduo.spider.db.repo.DoubanBookRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by lc on 2020/1/15
 */
@Service
public class DoubanBookService {

    @Autowired
    DoubanBookRepo repo;

    @Autowired
    ChildrenBookService childrenBookService;

    public DoubanBook saveOrUpdate(DoubanBook doubanBook) {
        if (StringUtils.isEmpty(doubanBook.getCode()) || StringUtils.isEmpty(doubanBook.getUrl()) || null == doubanBook.getPlatformId()) {
            return null;
        }

        DoubanBook old = repo.findByCodeAndPlatformIdAndUrl(doubanBook.getCode(), doubanBook.getPlatformId(),doubanBook.getUrl());

        if (null == old) {
            this.tagChildrenBook(doubanBook);
            return repo.save(doubanBook);
        }

        if (StringUtils.isNotEmpty(doubanBook.getBookName())) {
            old.setBookName(doubanBook.getBookName());
        }
        if (null != doubanBook.getScore()) {
            old.setScore(doubanBook.getScore());
        }
        if (null != doubanBook.getScorePerson()) {
            old.setScorePerson(doubanBook.getScorePerson());
        }
        if (null != doubanBook.getScore5Proportion()) {
            old.setScore5Proportion(doubanBook.getScore5Proportion());
        }
        if (null != doubanBook.getScore4Proportion()) {
            old.setScore4Proportion(doubanBook.getScore4Proportion());
        }
        if (null != doubanBook.getScore3Proportion()) {
            old.setScore3Proportion(doubanBook.getScore3Proportion());
        }
        if (null != doubanBook.getScore2Proportion()) {
            old.setScore2Proportion(doubanBook.getScore2Proportion());
        }
        if (null != doubanBook.getScore1Proportion()) {
            old.setScore1Proportion(doubanBook.getScore1Proportion());
        }
        if (null != doubanBook.getComment()) {
            old.setComment(doubanBook.getComment());
        }

        return repo.save(old);
    }


    private void tagChildrenBook(DoubanBook doubanBook) {
        childrenBookService.tagDoubanQueryStatus(doubanBook);
    }

    public List<DoubanBook> findAll() {
        List<DoubanBook> all = repo.findAll();
        return all;
    }

    public List<DoubanBook> findNotUpdateByDay(Date day) {
        return repo.findNotUpdateByDay(day);
    }
}
