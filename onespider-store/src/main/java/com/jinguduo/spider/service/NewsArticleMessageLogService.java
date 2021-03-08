package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.NewsArticleMessageLogs;
import com.jinguduo.spider.db.repo.NewsArticleMessageLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by gaozl on 2020/10/14.
 */
@Service
public class NewsArticleMessageLogService {
    @Autowired
    NewsArticleMessageLogRepo repo;

    public NewsArticleMessageLogs save(NewsArticleMessageLogs log) {
        if(log.getUrl().length()>254){
            return null;
        }

        log.setTitle(TextUtils.removeEmoji(log.getTitle()));
        NewsArticleMessageLogs oldLog = repo.findByCodeAndTitle(log.getCode(), log.getTitle()).orElse(null);

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
