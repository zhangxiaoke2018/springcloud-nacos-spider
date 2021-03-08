package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.WeiboIndexHourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public interface WeiboIndexHourLogRepo extends JpaRepository<WeiboIndexHourLog, Integer> {
    WeiboIndexHourLog findByCodeAndHour(String code, Date hour);
}
