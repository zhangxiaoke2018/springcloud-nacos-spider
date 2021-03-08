package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jinguduo.spider.data.table.AudioPlayCountLog;

public interface AudioPlayCountRepo extends JpaRepository<AudioPlayCountLog, Integer> {
	
}
