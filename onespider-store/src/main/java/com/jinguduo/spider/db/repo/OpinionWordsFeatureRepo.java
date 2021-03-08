package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.OpinionWordsFeature;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/06/2017 14:52
 */
@Repository
public interface OpinionWordsFeatureRepo extends JpaRepository<OpinionWordsFeature, Integer> {



}
