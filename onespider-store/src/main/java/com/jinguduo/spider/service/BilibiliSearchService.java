package com.jinguduo.spider.service;


import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.db.repo.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 13:43
 */
@Service
@Slf4j
public class BilibiliSearchService {

    @Autowired
    private BilibiliVideoClickRepo bilibiliVideoClickRepo;

    @Autowired
    private BilibiliVideoDmRepo bilibiliVideoDmRepo;

    @Autowired
    private BilibiliVideoStowRepo bilibiliVideoStowRepo;

    @Autowired
    private BilibiliVideoCountRepo bilibiliVideoCountRepo;

    @Autowired
    private BilibiliVideoScoreRepo bilibiliVideoScoreRepo;

    @Autowired
    private BilibiliFansRepo bilibiliFansRepo;


    public BilibiliVideoScore insertOrUpdated(BilibiliVideoScore b){
        BilibiliVideoScore old = bilibiliVideoScoreRepo.findByCodeAndDayAndScoreAndScoreNumber(b.getCode(),b.getDay(),b.getScore(),b.getScoreNumber());
        if(null != old){
            old.setScore(b.getScore());
            old.setScoreNumber(b.getScoreNumber());
            old.setCrawledAt(new Timestamp(System.currentTimeMillis()));
            bilibiliVideoScoreRepo.save(old);
        }else{
            return bilibiliVideoScoreRepo.save(b);
        }
        return null;
    }



    public BilibiliVideoClick saveClick(BilibiliVideoClick click) {

        return bilibiliVideoClickRepo.save(click);
    }

    public BilibiliVideoDm saveDm(BilibiliVideoDm dm) {

        return bilibiliVideoDmRepo.save(dm);
    }

    public BilibiliVideoStow saveStow(BilibiliVideoStow stow) {

        return bilibiliVideoStowRepo.save(stow);
    }

    public Object saveCount(BilibiliVideoCount count) {
        return bilibiliVideoCountRepo.save(count);
    }

    public Object saveFans(BilibiliFansCount bangumi) {

        return bilibiliFansRepo.save(bangumi);
    }

}
