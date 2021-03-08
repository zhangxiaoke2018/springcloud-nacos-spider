package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.Index360Logs;

import java.util.Date;

/**
 * Created by lc on 2017/5/5.
 */
public interface Index360LogRepo extends JpaRepository<Index360Logs, Integer> {
    Index360Logs findByCodeAndIndexDay(String code, Date indexDay);
}
