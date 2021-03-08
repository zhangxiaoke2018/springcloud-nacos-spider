package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WeiboFeedKeywordTag;
import com.jinguduo.spider.db.repo.WeiboFeedKeywordTagRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 08/08/2017 10:57
 */
@Service
@CommonsLog
public class WeiboFeedKeywordTagService {

    @Autowired
    private WeiboFeedKeywordTagRepo weiboFeedKeywordTagRepo;


    public void save(Collection<WeiboFeedKeywordTag> weiboFeedKeywordTags) {

        for (WeiboFeedKeywordTag weiboFeedKeywordTag : weiboFeedKeywordTags) {
            if (null == weiboFeedKeywordTag.getDay()) continue;

            WeiboFeedKeywordTag wfkt = weiboFeedKeywordTagRepo.findByKeywordAndTagAndDay(weiboFeedKeywordTag.getKeyword(), weiboFeedKeywordTag.getTag(), DateFormatUtils.format(weiboFeedKeywordTag.getDay(), "yyyy-MM-dd"));

            if (wfkt == null) {
                weiboFeedKeywordTagRepo.save(weiboFeedKeywordTag);
            }

        }

    }

    public void save(WeiboFeedKeywordTag weiboFeedKeywordTags) {

        if (null == weiboFeedKeywordTags.getDay()) {
            return;
        }

        WeiboFeedKeywordTag wfkt = weiboFeedKeywordTagRepo.findByKeywordAndTagAndDay(weiboFeedKeywordTags.getKeyword(), weiboFeedKeywordTags.getTag(), DateFormatUtils.format(weiboFeedKeywordTags.getDay(), "yyyy-MM-dd"));

        if (wfkt == null) {
            weiboFeedKeywordTagRepo.save(weiboFeedKeywordTags);
        }


    }

}
