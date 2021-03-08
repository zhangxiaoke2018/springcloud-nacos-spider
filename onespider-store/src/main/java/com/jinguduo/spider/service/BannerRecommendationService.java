package com.jinguduo.spider.service;

import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.db.repo.BannerRecommendationRepo;

@Service
public class BannerRecommendationService {
	
	@Autowired
	private BannerRecommendationRepo bannerRepo;


	public String saveBanner(BannerRecommendation item) {
		if (!checkObject(item))
			return "INVALID ITEM";

		item = bannerRepo.save(item);
		
		return null!=item?"SUCCESS":"FAILURE";
	}

	private boolean checkObject(Object item) {
		if (item instanceof BannerRecommendation) {
			BannerRecommendation banner = (BannerRecommendation) item;
			return StringUtils.hasText(banner.getCode()) 
					&& banner.getPlatformId() != null
					&& banner.getBannerType() > 0
					&& banner.getPlatformId() > 0;
		}
		return false;
	}
}
