package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionChapters;

/**
 * 
 * @author huhu
 *
 */
public interface FictionChapterRepo extends JpaRepository<FictionChapters, Integer> {
	
	FictionChapters findByCodeAndPlatformIdAndDay(String code,Integer platformId,Date day);
	
}
