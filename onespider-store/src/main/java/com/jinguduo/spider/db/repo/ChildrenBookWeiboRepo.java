package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookWeibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2020/2/17
 */
@Repository
public interface ChildrenBookWeiboRepo extends JpaRepository<ChildrenBookWeibo, Integer> {
    ChildrenBookWeibo findFirstByMidAndCodeAndPlatformId(String mid,String code,Integer platformId);
}
