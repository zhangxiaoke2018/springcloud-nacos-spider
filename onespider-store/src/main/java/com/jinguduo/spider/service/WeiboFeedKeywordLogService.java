package com.jinguduo.spider.service;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.WeiboFeedKeywordLog;
import com.jinguduo.spider.db.repo.WeiboFeedKeywordLogRepo;

import lombok.extern.apachecommons.CommonsLog;

@Service
@CommonsLog
public class WeiboFeedKeywordLogService {
    
    @Resource
    private WeiboFeedKeywordLogRepo weiboFeedKeywordLogRepo;

    public boolean save(Collection<WeiboFeedKeywordLog> items) {
        boolean r = true;
        for (WeiboFeedKeywordLog item : items) {
            try {
                WeiboFeedKeywordLog n = weiboFeedKeywordLogRepo.findFirstByTypeAndRelevanceIdAndDayOrderById(item.getType(), item.getRelevanceId(), item.getDay());
                if (n != null) {
                    DbEntityHelper.copy(item, n, new String[]{"id", "type", "relevanceId", "day"});
                    item = n;
                }
                weiboFeedKeywordLogRepo.save(item);
                
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                r = false;
            }
        }
        return r;
    }

}
