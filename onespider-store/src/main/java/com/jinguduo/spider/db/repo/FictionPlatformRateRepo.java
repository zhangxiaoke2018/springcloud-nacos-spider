package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionPlatformRate;

/**
 * 
 * @author huhu
 *
 */
public interface FictionPlatformRateRepo extends JpaRepository<FictionPlatformRate, Integer> {
	FictionPlatformRate findByCodeAndPlatformIdAndDay(String code,Integer platformId,Date day);
}
