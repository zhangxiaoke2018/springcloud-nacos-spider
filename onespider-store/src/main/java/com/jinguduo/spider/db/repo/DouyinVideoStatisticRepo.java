package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinVideoStatistic;

@Component
public interface DouyinVideoStatisticRepo extends CrudRepository<DouyinVideoStatistic, Long> {

}
