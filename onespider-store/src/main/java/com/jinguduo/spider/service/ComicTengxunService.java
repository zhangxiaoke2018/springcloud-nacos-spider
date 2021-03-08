package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicTengxun;
import com.jinguduo.spider.data.table.ComicTengxunComment;
import com.jinguduo.spider.db.repo.ComicTengxunCommentRepo;
import com.jinguduo.spider.db.repo.ComicTengxunRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/2
 * Time:19:25
 */
@Service
@Slf4j
public class ComicTengxunService {

    @Autowired
    ComicTengxunRepo repo;
    @Autowired
    ComicTengxunCommentRepo commentRepo;


    public ComicTengxun saveOrUpdate(ComicTengxun logs) {
        if (null == logs) return null;
        if (StringUtils.isBlank(logs.getCode()) || null == logs.getDay()) {
            return null;
        }
        if (null != logs.getCommentNum()) {
            ComicTengxunComment ctc = new ComicTengxunComment();
            ctc.setCode(logs.getCode());
            ctc.setCommentNum(logs.getCommentNum());
            commentRepo.save(ctc);
        }
        boolean isSkip = skipByCode(logs.getCode());
        if (isSkip) {
            return logs;
        }
        ComicTengxun ct = repo.findByComicIdAndDay(logs.getComicId(), logs.getDay());
        //如果有该数据,插入id
        if (null != ct) {
            try {
                if (null != logs.getCode()) {
                    ct.setCode(logs.getCode());
                }
                if (null != logs.getCollectNum() && logs.getCollectNum() > 0) {
                    ct.setCollectNum(logs.getCollectNum());
                }
                if (null != logs.getHotNum() && logs.getHotNum() > 0) {
                    ct.setHotNum(logs.getHotNum());
                }
                if (null != logs.getPraiseNum() && logs.getPraiseNum() > 0) {
                    ct.setPraiseNum(logs.getPraiseNum());
                }
                if (null != logs.getCommentNum() && logs.getCommentNum() > 0) {
                    ct.setCommentNum(logs.getCommentNum());
                }
                if (null != logs.getScoreNum() && logs.getScoreNum() > 0) {
                    ct.setScoreNum(logs.getScoreNum());
                }
                if (null != logs.getScoreCount() && logs.getScoreCount() > 0) {
                    ct.setScoreCount(logs.getScoreCount());
                }
                if (null != logs.getWeeklyTicketNum() && logs.getWeeklyTicketNum() > 0) {
                    ct.setWeeklyTicketNum(logs.getWeeklyTicketNum());
                }
                if (null != logs.getWeeklyTicketRank() && logs.getWeeklyTicketRank() > 0) {
                    ct.setWeeklyTicketRank(logs.getWeeklyTicketRank());
                }
                return repo.save(ct);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            return repo.save(logs);
        }
        return null;
    }


}
