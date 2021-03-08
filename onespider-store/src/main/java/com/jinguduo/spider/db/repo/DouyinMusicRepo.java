package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinMusic;

@Component
public interface DouyinMusicRepo extends CrudRepository<DouyinMusic, Long> {

	DouyinMusic findOneByMid(Long mid);

}
