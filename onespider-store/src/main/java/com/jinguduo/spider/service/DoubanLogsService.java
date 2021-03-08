package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.DoubanCommentsText;
import com.jinguduo.spider.data.table.DoubanLog;
import com.jinguduo.spider.db.repo.DoubanCommentsTextRepo;
import com.jinguduo.spider.db.repo.DoubanLogsRepo;
import lombok.extern.apachecommons.CommonsLog;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 15:29
 */
@Service
@CommonsLog
public class DoubanLogsService {

    @Autowired
    private DoubanLogsRepo doubanLogsRepo;

    @Autowired
    private DoubanCommentsTextRepo doubanCommentsTextRepo;

    public DoubanLog insertOrUpdate(DoubanLog log){

        if(log == null || StringUtils.isBlank(log.getCode()) || log.getBriefComment() == -1){
            return null;
        }

        return doubanLogsRepo.save(log);
    }

    private final static long DELTA = TimeUnit.HOURS.toMillis(12);
    public DoubanCommentsText addOrUpdateComment(DoubanCommentsText doubanCommentsText) {

        DoubanCommentsText text = null;

        if (doubanCommentsText == null) {
            return null;
        }

        DoubanCommentsText dct = doubanCommentsTextRepo.findOneByCommentId(doubanCommentsText.getCommentId());
        if (dct != null && (System.currentTimeMillis() - dct.getUpdatedAt().getTime()) < DELTA) {
            // 跳过近期更新过内容： 过多的DB写操作，阻塞后给Store ECS CPU造成过高使用率
            return doubanCommentsText;
        }

        doubanCommentsText.setNickName(TextUtils.removeExpression(doubanCommentsText.getNickName()));
        doubanCommentsText.setContent(TextUtils.removeExpression(doubanCommentsText.getContent()));
        if (dct == null){
            text = doubanCommentsTextRepo.save(doubanCommentsText);
        } else {
            DbEntityHelper.copy(doubanCommentsText, dct, new String[]{"id", "code", "created_at"});
            text = doubanCommentsTextRepo.save(dct);
        }

        return text;
    }
}
