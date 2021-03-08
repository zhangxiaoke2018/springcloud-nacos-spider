package com.jinguduo.spider.db.repo;

import java.sql.Timestamp;
import java.util.Collection;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.Seed;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@Component
public interface SeedRepo extends PagingAndSortingRepository<Seed,Integer> {

    Seed findByUrl(String url);
    
    Seed findByUrlAndStatus(String url,Integer status);
    
    Collection<Seed> findAllByStatusAndUpdatedAtGreaterThanEqualOrderByFrequencyAscPriorityDesc(Integer status,Timestamp timestamp);
    
    Seed findByCode(String code);
    
    Seed findByCodeAndStatus(String code,Integer status);

}
