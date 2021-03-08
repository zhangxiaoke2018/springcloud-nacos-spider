package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.WechatArticleKeywordLog;
import com.jinguduo.spider.db.repo.WechatArticleKeywordLogRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

@Service
@CommonsLog
public class WechatArticleKeywordLogService {

    @Resource
    private WechatArticleKeywordLogRepo wechatArticleKeywordLogRepo;

    public boolean save(Collection<WechatArticleKeywordLog> items) {
        boolean r = true;
        for (WechatArticleKeywordLog item : items) {
            try {
                if (StringUtils.isEmpty(item.getKeyword()) || item.getKeyword().length() > 63) {
                    continue;
                }
                WechatArticleKeywordLog n = wechatArticleKeywordLogRepo.findFirstByKeywordAndDayOrderById(item.getKeyword(), item.getDay());
                if (n != null) {
                    DbEntityHelper.copy(item, n, new String[]{"id", "keyword", "day"});
                    item = n;
                }
                wechatArticleKeywordLogRepo.save(item);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                r = false;
            }
        }
        return r;
    }

}
