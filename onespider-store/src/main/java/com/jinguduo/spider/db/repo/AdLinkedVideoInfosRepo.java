package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.AdLinkedVideoInfos;

@Deprecated
public interface AdLinkedVideoInfosRepo extends JpaRepository<AdLinkedVideoInfos, Integer> {
	AdLinkedVideoInfos findByCodeAndPlatformId(String code,Integer platformId);
}
