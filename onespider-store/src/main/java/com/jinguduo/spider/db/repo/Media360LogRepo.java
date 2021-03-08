package com.jinguduo.spider.db.repo;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.Media360Logs;

/**
 * Created by lc on 2017/5/10.
 */
public interface Media360LogRepo extends JpaRepository<Media360Logs, Integer> {
    Media360Logs findByCodeAndMediaDay(String code, Date mediaDay);
}
