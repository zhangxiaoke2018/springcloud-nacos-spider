package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.AutoFindLogs;

@Repository
public interface AutoFindLogsRepo extends JpaRepository<AutoFindLogs, Integer>{

}
