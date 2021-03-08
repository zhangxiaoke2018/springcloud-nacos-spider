package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.ShowLog;

import java.sql.Timestamp;
import java.util.List;

@Component
public interface ShowLogRepo extends JpaRepository<ShowLog, Integer> {

    List<ShowLog> findTop24ByCodeOrderByIdDesc(String code);

    ShowLog findTop1ByCodeAndCrawledAtBetweenOrderByCrawledAtDesc(String code, Timestamp startCrawledAt,Timestamp endCrawledAt);

    List<ShowLog> findByCodeAndCrawledAtBetween(String code,Timestamp startCrawledAt,Timestamp endCrawledAt);
    
    //code 可能有重复，加平台
    List<ShowLog> findByCodeAndPlatformIdAndCrawledAtBetween(String code,Integer platformId,Timestamp startCrawledAt,Timestamp endCrawledAt);
}
