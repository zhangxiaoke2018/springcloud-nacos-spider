package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicBestSellingRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by lc on 2019/4/17
 */
@Repository
public interface ComicBestSellingRankRepo extends JpaRepository<ComicBestSellingRank, Integer> {
    ComicBestSellingRank findByPlatformIdAndDayAndRank(Integer platformId, Date day,Integer rank);
}
