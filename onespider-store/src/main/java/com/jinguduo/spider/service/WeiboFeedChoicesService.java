package com.jinguduo.spider.service;

import com.alibaba.fastjson.JSON;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.WeiboFeedChoices;
import com.jinguduo.spider.db.repo.WeiboFeedChoicesRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/03/2017 17:53
 */
@Service
@CommonsLog
public class WeiboFeedChoicesService {

    @Autowired
    private WeiboFeedChoicesRepo weiboFeedChoicesRepo;


    public boolean save(Collection<WeiboFeedChoices> items) {
        boolean r = true;
        for (WeiboFeedChoices item : items) {
            try {
                WeiboFeedChoices n = weiboFeedChoicesRepo.findByKeywordAndPostTimeAndNickName(item.getKeyword(),item.getPostTime(),item.getNickName());
                if (n != null) {
                    DbEntityHelper.copy(item, n, new String[]{"id", "keyword", "nickName","postTime"});
                    item = n;
                }
                String content = item.getContent().replaceAll("[\\x{10000}-\\x{10FFFF}]", "");
                item.setContent(content);
                weiboFeedChoicesRepo.save(item);

            } catch (Exception e) {
                log.error(JSON.toJSON(item) + e.getMessage(), e);
                r = false;
            }
        }
        return r;
    }


}
