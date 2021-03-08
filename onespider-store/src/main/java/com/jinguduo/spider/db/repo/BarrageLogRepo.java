package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.BarrageLog;

/**
 * Created by csonezp on 2016/10/28.
 */
public interface BarrageLogRepo extends JpaRepository<BarrageLog,Integer> {
}
