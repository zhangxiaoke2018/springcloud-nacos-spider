package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ToutiaoNewLogs;
import com.jinguduo.spider.db.repo.NewsToutiaoRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2017/5/15.
 */
@Service
@Slf4j
public class NewsToutiaoLogsService {

    @Autowired
    private NewsToutiaoRepo toutiaoRepo;


    public ToutiaoNewLogs save(ToutiaoNewLogs logs) {
        if (null == logs) {
            log.error("***save ToutiaoNewLogs error ,because ToutiaoNewLogs is null ***");
            return null;
        }
        ToutiaoNewLogs logsByCodeAndId = toutiaoRepo.findByCodeAndToutiaoId(logs.getCode(), logs.getToutiaoId());
        //如果有该数据,插入id   findByRelevanceIdAndRelevanceIdAndIndexDay
        if (null != logsByCodeAndId) {
            logsByCodeAndId.setSourceUrl(logs.getSourceUrl());
            logsByCodeAndId.setAuthor(logs.getAuthor());
            logsByCodeAndId.setTitle(logs.getTitle());
            logsByCodeAndId.setCommentsCount(logs.getCommentsCount());
            logsByCodeAndId.setNewsDate(logs.getNewsDate());
            logs = logsByCodeAndId;
        }
        if (logs.getSourceUrl().length() > 255) {
        	logs.setSourceUrl(logs.getSourceUrl().substring(0, 255));
		}
        return toutiaoRepo.save(logs);
    }
}
