package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jinguduo.spider.data.table.AudioVolumeLog;

public interface AudioVolumeRepo extends JpaRepository<AudioVolumeLog, Integer> {
	
	AudioVolumeLog findOneByCodeAndPlatformIdAndDay(String code,Integer platformId,Date day);
	
}
