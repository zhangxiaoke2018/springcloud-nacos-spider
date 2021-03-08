package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicBilibili;
import com.jinguduo.spider.db.repo.ComicBilibiliRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * Created by lc on 2018/9/10
 */
@Service
@Slf4j
public class ComicBilibiliService {
    @Autowired
    private ComicBilibiliRepo repo;

    public ComicBilibili insertOrUpdate(ComicBilibili bilibili) {
        boolean isSkip = skipByCode(bilibili.getCode());
        if (isSkip) {
            return bilibili;
        }

        ComicBilibili old = repo.findByCodeAndDay(bilibili.getCode(), bilibili.getDay());
        //如果有该数据,插入id
        if (null != old) {
            if (bilibili.getCommentCount() != null && bilibili.getCommentCount() > 0) {
                old.setCommentCount(bilibili.getCommentCount());
            }
            if (bilibili.getMonthTickets() != null && bilibili.getMonthTickets() > 0) {
                old.setMonthTickets(bilibili.getMonthTickets());
            }
            if (bilibili.getFans() != null && bilibili.getFans() > 0) {
                old.setFans(bilibili.getFans());
            }
            repo.save(old);
        } else {
            return repo.save(bilibili);
        }
        return null;
    }
}
