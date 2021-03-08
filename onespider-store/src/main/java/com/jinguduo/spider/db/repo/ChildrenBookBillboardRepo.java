package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookBillboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by lc on 2019/12/5
 */
@Repository
public interface ChildrenBookBillboardRepo extends JpaRepository<ChildrenBookBillboard, Integer> {
    ChildrenBookBillboard findByDayAndPlatformIdAndTypeAndRank(Date day,Integer platformId,String type,Integer rank);
}
