package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinChallenge;

@Component
public interface DouyinChallengeRepo extends CrudRepository<DouyinChallenge, Long> {

	DouyinChallenge findOneByCid(Long cId);

}
