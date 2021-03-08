package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionPlatformClick;

/**
 * 
 * @author huhu
 *
 */
public interface FictionPlatformClickRepo extends JpaRepository<FictionPlatformClick, Integer> {
	FictionPlatformClick findByCodeAndPlatformIdAndDay(String code,Integer platformId,Date day);
}
