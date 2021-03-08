package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.NewsArticleLog;
import com.jinguduo.spider.db.repo.NewsArticleLogRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by csonezp on 2017/3/10.
 */
@Service
public class NewsArticleLogService {
    @Autowired
    NewsArticleLogRepo repo;

    public NewsArticleLog save(NewsArticleLog log) {
        if(log.getUrl().length()>254){
            return null;
        }

        log.setTitle(TextUtils.removeEmoji(log.getTitle()));
        NewsArticleLog oldLog = repo.findByCodeAndTitle(log.getCode(), log.getTitle()).orElse(null);

        if (oldLog == null) {
            oldLog = repo.findByCodeAndUrl(log.getCode(), log.getUrl()).orElse(null);
        }
        //如果查询到老的存在，则更新
        if (oldLog != null) {
            DbEntityHelper.copy(log, oldLog, new String[]{"id", "url", "code"});
            log = oldLog;
        }
        return repo.save(log);
    }
}
