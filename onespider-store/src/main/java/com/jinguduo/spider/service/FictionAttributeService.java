package com.jinguduo.spider.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.FictionChapters;
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformFavorite;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.data.table.FictionPlatformRecommend;
import com.jinguduo.spider.db.repo.FictionChapterRepo;
import com.jinguduo.spider.db.repo.FictionPlatformClickRepo;
import com.jinguduo.spider.db.repo.FictionPlatformFavoriteRepo;
import com.jinguduo.spider.db.repo.FictionPlatformRateRepo;
import com.jinguduo.spider.db.repo.FictionPlatformRecommendRepo;

@Service
public class FictionAttributeService {
	@Autowired
	private FictionPlatformClickRepo fictionClickRepo;
	@Autowired
	private FictionPlatformFavoriteRepo fictionFavoriteRepo;
	@Autowired
	private FictionPlatformRecommendRepo fictionRecommendRepo;
	@Autowired
	private FictionPlatformRateRepo fictionRateRepo;
	@Autowired
	private FictionChapterRepo fictionChapterRepo;

	public FictionPlatformClick save(FictionPlatformClick item) {
		if(item.getCode()==null
				||item.getDay()==null
				||item.getPlatformId()==null
				||item.getClickCount()==null)
			return null;
		
		FictionPlatformClick old = fictionClickRepo.findByCodeAndPlatformIdAndDay(item.getCode(), item.getPlatformId(),item.getDay());
		if(old!=null) {
			item.setId(old.getId());
		}
		return fictionClickRepo.save(item);
	}
	
	public FictionPlatformFavorite save(FictionPlatformFavorite item) {
		if(item.getCode()==null
				||item.getDay()==null
				||item.getPlatformId()==null
				||item.getFavoriteCount()==null)
			return null;
		
		FictionPlatformFavorite old = fictionFavoriteRepo.findByCodeAndPlatformIdAndDay(item.getCode(),item.getPlatformId(),item.getDay());
		if(old!=null) {
			item.setId(old.getId());
			if(item.getFavoriteCount()<=0 && old.getFavoriteCount()>0)
				return null;
		}
		return fictionFavoriteRepo.save(item);
	}
	
	public FictionPlatformRecommend save(FictionPlatformRecommend item) {
		if(item.getCode()==null
				||item.getDay()==null
				||item.getPlatformId()==null
				||item.getRecommendCount()==null)
			return null;
		
		FictionPlatformRecommend old = fictionRecommendRepo.findByCodeAndPlatformIdAndDay(item.getCode(),item.getPlatformId(),item.getDay());
		if(old!=null) {
			item.setId(old.getId());
		}
		return fictionRecommendRepo.save(item);
	}
	
	public FictionPlatformRate save(FictionPlatformRate item) {
		if(item.getCode()==null
				||item.getDay()==null
				||item.getPlatformId()==null
				||item.getUserCount()==null
				||item.getRate()==null)
			return null;
		
		FictionPlatformRate old = fictionRateRepo.findByCodeAndPlatformIdAndDay(item.getCode(),item.getPlatformId(),item.getDay());
		if(old!=null) {
			item.setId(old.getId());
		}
		return fictionRateRepo.save(item);
	}
	

	public FictionChapters save(FictionChapters newItem) {
		if(StringUtils.isEmpty(newItem.getCode())
				||newItem.getPlatformId()==null)
			return null;
		
		FictionChapters exist = fictionChapterRepo.findByCodeAndPlatformIdAndDay(newItem.getCode(), newItem.getPlatformId(), newItem.getDay());
		
		if (exist != null) {
			newItem.setId(exist.getId());
		}

		return fictionChapterRepo.save(newItem);
	}
}
