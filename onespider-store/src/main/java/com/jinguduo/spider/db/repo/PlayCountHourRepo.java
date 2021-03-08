package com.jinguduo.spider.db.repo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.PlayCountHour;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月28日 下午4:39:10
 *
 */
@Repository
public interface PlayCountHourRepo extends JpaRepository<PlayCountHour, Integer> {
    Optional<List<PlayCountHour>> findByShowIdAndCrawledAtGreaterThanAndCrawledAtLessThan(Integer showId, Timestamp crawledAt1, Timestamp crawledAt2);
    
    PlayCountHour findByShowIdAndPlatformIdAndCrawledAt(Integer showId, Integer platformId, Timestamp crawledAt);
}
