package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.FictionOriginalBillboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

/**
 * Created by lc on 2019/6/27
 */
public interface FictionOriginalBillboardRepo extends JpaRepository<FictionOriginalBillboard, Integer> {
    FictionOriginalBillboard findByPlatformIdAndTypeAndDayAndRank(Integer platformId, String type, Date day, Integer rank);
}
