package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.JianshuBookLogs;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by lc on 2020/1/17
 */
public interface JianshuBookLogsRepo extends JpaRepository<JianshuBookLogs, Integer> {
    JianshuBookLogs findByKeywordAndCode(String keyword, String code);
}
