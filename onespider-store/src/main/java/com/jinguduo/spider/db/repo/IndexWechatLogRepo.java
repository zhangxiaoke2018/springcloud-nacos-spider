package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.IndexWechatLogs;

import java.util.Date;

/**
 * Created by lc on 2017/5/5.
 */
public interface IndexWechatLogRepo extends JpaRepository<IndexWechatLogs, Integer> {
    IndexWechatLogs findByCodeAndIndexDay(String code, Date indexDay);

    IndexWechatLogs findByIndexDay(Date indexDay);
}
