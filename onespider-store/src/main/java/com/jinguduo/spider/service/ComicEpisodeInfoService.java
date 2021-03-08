package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.db.repo.ComicEpisodeInfoRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/3/14
 */
@Service
public class ComicEpisodeInfoService {

    @Autowired
    ComicEpisodeInfoRepo infoRepo;

    public ComicEpisodeInfo insertOrUpdate(ComicEpisodeInfo info) {
        if (StringUtils.isEmpty(info.getCode()) || null == info.getDay()) {
            return info;
        }
        if (info.getVipStatus() == null){
            info.setVipStatus(0);
        }
        ComicEpisodeInfo old = null;
        //如果有分集，则根据分集查询旧数据
        if (null != info.getEpisode()) {
            old = infoRepo.findByCodeAndEpisodeAndDay(info.getCode(), info.getEpisode(), info.getDay());
        } else if (!StringUtils.isEmpty(info.getChapterId())) {
            //如果没有分集，则根据chapterId 查旧数据
            old = infoRepo.findByCodeAndChapterIdAndDay(info.getCode(), info.getChapterId(), info.getDay());
            //如果没有，则丢弃，如果有则继续执行
            if (null == old) {
                return info;
            }
        } else {
            return info;
        }

        if (null == old) {
            infoRepo.save(info);
        } else {
            if (old.getComment() == null ){
                old.setComment(info.getComment());
            }
            if (old.getLikeCount() == null){
                old.setLikeCount(info.getLikeCount());
            }
            infoRepo.save(old);
        }
        return info;
    }
}
