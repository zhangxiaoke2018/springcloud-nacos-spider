package com.jinguduo.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.DouyinVideo;
import com.jinguduo.spider.db.repo.DouyinVideoRepo;

@Service
public class DouyinVideoService {
	
	@Autowired
	private DouyinVideoRepo douyinVideoRepo;

	public Iterable<DouyinVideo> save(List<DouyinVideo> videos) {
		if (videos == null || videos.isEmpty()) {
			return null;
		}
		return douyinVideoRepo.save(videos);
	}

}
