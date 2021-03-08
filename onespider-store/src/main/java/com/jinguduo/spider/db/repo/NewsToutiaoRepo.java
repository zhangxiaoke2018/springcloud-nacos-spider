package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.ToutiaoNewLogs;

/**
 * Created by lc on 2017/5/15.
 */
public interface NewsToutiaoRepo extends JpaRepository<ToutiaoNewLogs, Integer> {
    ToutiaoNewLogs findByCodeAndToutiaoId(String code, String toutiaoId);
}
