package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionIncomeLogs;

/**
 * 
 * @author huhu
 *
 */
public interface FictionIncomeLogsRepo extends JpaRepository<FictionIncomeLogs, Integer> {

	FictionIncomeLogs findByIncomeIdAndCodeAndDay(Integer incomeId,String code,Date day);
}
