package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.WechatArticleChoice;
import com.jinguduo.spider.db.repo.WechatArticleChoiceRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class WechatArticleChoiceService {
    
    @Autowired
    private WechatArticleChoiceRepo wechatArticleChoiceRepo;

    public boolean save(Collection<WechatArticleChoice> items) {
        boolean r = true;
        for (WechatArticleChoice item : items) {
            if(StringUtils.isBlank(item.getTitle())){
                continue;
            }
            try {
                WechatArticleChoice n = wechatArticleChoiceRepo.findFirstByTypeAndRelevanceIdAndTitleAndDay(item.getType(),item.getRelevanceId(),item.getTitle(),item.getDay());
                if (n != null) {
                    DbEntityHelper.copy(item, n, new String[]{"id", "type", "relevanceId","title","day"});
                    item = n;
                }
                String summary =item.getSummary();
                summary = summary.replaceAll("[\\x{10000}-\\x{10FFFF}]", "");
                item.setSummary(summary);
                wechatArticleChoiceRepo.save(item);
                
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                r = false;
            }
        }
        return r;
    }

    public Set<String> findNoReadCount(String day){

        Date yesterdayZero = DateHelper.getYesterdayZero(Date.class);
        day = DateFormatUtils.format(yesterdayZero, "yyyy-MM-dd");
        Set<String> wechatArticleChoices = wechatArticleChoiceRepo.findByDayAndReadCount(day, 0)
                .stream().map(w -> w.getUrl()+"#"+w.getId().toString()).collect(Collectors.toSet());

        return wechatArticleChoices;
    }

}
