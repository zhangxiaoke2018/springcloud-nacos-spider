package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.FictionCodeRelation;

public interface FictionCodeRelationRepo extends JpaRepository<FictionCodeRelation,Integer>{
	FictionCodeRelation findByCodeAndPlatformId(String code,Integer platformId);
}
