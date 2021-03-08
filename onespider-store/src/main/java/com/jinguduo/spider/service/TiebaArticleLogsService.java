package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.TiebaArticleLogs;
import com.jinguduo.spider.db.repo.TiebaArticleLogsRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 21/04/2017 10:35
 */
@Service
@CommonsLog
public class TiebaArticleLogsService {

    @Autowired
    private TiebaArticleLogsRepo tiebaArticleLogsRepo;

    public TiebaArticleLogs insertOrUpdate(TiebaArticleLogs tiebaArticleLogs){

        if(tiebaArticleLogs == null|| org.apache.commons.lang3.StringUtils.isBlank(tiebaArticleLogs.getTitle()) ) return null;

        TiebaArticleLogs ta = null;

        TiebaArticleLogs dbLog = tiebaArticleLogsRepo.findByCodeAndUrl(tiebaArticleLogs.getCode() ,tiebaArticleLogs.getUrl());

        tiebaArticleLogs.setTitle(TextUtils.removeExpression(tiebaArticleLogs.getTitle()));
        if(StringUtils.isEmpty(tiebaArticleLogs.getTitle())){
            return null;
        }
        if(dbLog != null){

            dbLog.setTitle(tiebaArticleLogs.getTitle());
            dbLog.setRepNum(tiebaArticleLogs.getRepNum());
            ta = tiebaArticleLogsRepo.save(dbLog);

        }else {
            ta = tiebaArticleLogsRepo.save(tiebaArticleLogs);
        }
        return ta;
    }


}
