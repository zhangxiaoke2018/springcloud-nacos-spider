package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ShowPopularLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Created by lc on 2018/9/3
 */
@Component
public interface ShowPopularLogsRepo extends JpaRepository<ShowPopularLogs, Integer> {
}
