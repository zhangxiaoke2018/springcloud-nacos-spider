package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jinguduo.spider.data.table.Audio;

public interface AudioRepo extends JpaRepository<Audio, Integer> {
	
	Audio findOneByCodeAndPlatformId(String code,Integer platformId);
	
}
