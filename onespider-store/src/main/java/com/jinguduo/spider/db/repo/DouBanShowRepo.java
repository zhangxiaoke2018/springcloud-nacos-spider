package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouBanShow;

import java.util.Optional;

/**
 * Created by csonezp on 2016/8/15.
 */
@Component
public interface DouBanShowRepo extends PagingAndSortingRepository<DouBanShow, Integer> {
    DouBanShow findByCode(String code);


    DouBanShow findById(Integer showId);
}
