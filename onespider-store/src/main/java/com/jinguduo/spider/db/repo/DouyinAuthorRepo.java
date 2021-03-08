package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinAuthor;

@Component
public interface DouyinAuthorRepo extends CrudRepository<DouyinAuthor, Long> {

	DouyinAuthor findOneByUserId(Long userId);

}
