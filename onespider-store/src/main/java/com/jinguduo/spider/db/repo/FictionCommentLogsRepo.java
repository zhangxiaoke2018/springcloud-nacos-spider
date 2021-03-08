package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionCommentLogs;

/**
 * 
 * @author huhu
 *
 */
public interface FictionCommentLogsRepo extends JpaRepository<FictionCommentLogs, Integer> {
	
	FictionCommentLogs findByCodeAndPlatformIdAndDay(String code,Integer platformId,Date day);
	
}
