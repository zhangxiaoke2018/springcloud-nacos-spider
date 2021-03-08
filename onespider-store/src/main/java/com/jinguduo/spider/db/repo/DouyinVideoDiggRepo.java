package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinVideoDigg;

@Component
public interface DouyinVideoDiggRepo extends CrudRepository<DouyinVideoDigg, Long> {

	DouyinVideoDigg findOneByAwemeIdAndUserId(Long awemeId, Long userId);

}
