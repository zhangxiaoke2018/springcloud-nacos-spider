package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jinguduo.spider.data.table.BannerRecommendation;

public interface BannerRecommendationRepo extends JpaRepository<BannerRecommendation, Integer> {
	
}
