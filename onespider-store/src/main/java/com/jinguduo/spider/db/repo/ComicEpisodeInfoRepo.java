package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by lc on 2019/3/14
 */
@Repository
public interface ComicEpisodeInfoRepo extends JpaRepository<ComicEpisodeInfo, Integer> {
    ComicEpisodeInfo findByCodeAndEpisodeAndDay(String code, Integer episode, Date day);

    ComicEpisodeInfo findByCodeAndChapterIdAndDay(String code, String chapterId, Date day);



}
