package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionPlatformRecommend;

/**
 * 
 * @author huhu
 *
 */
public interface FictionPlatformRecommendRepo extends JpaRepository<FictionPlatformRecommend, Integer> {
	FictionPlatformRecommend findByCodeAndPlatformIdAndDay(String code,Integer platformId,Date day);
}
