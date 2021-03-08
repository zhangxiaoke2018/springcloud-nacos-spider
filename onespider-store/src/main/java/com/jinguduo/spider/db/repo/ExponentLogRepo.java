package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.ExponentLog;

@Component
public interface ExponentLogRepo extends JpaRepository<ExponentLog, Integer> {
    ExponentLog findByCodeAndExponentDate(String code,Date day);
}
