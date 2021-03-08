package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicOriginalBillboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by lc on 2019/5/29
 */
@Repository
public interface ComicOriginalBillboardRepo extends JpaRepository<ComicOriginalBillboard, Integer> {
    ComicOriginalBillboard findByDayAndPlatformIdAndBillboardTypeAndRank(Date day, Integer platformId, String billboardType, Integer rank);
}
