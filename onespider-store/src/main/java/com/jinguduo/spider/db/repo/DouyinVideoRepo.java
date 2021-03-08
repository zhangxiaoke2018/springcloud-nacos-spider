package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinVideo;

@Component
public interface DouyinVideoRepo extends CrudRepository<DouyinVideo, Long> {

	boolean existsByAwemeId(Long awemeId);

}
