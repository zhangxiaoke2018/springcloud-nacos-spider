package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionPlatformFavorite;

/**
 * 
 * @author huhu
 *
 */
public interface FictionPlatformFavoriteRepo extends JpaRepository<FictionPlatformFavorite, Integer> {
	FictionPlatformFavorite findByCodeAndPlatformIdAndDay(String code,Integer platformId,Date day);
}
