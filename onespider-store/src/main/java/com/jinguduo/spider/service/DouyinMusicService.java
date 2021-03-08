package com.jinguduo.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.DouyinMusic;
import com.jinguduo.spider.db.repo.DouyinMusicRepo;

@Service
public class DouyinMusicService {
	
	@Autowired
	private DouyinMusicRepo douyinMusicRepo;

	public Iterable<DouyinMusic> save(List<DouyinMusic> musics) {
		if (musics == null || musics.isEmpty()) {
			return null;
		}
		return douyinMusicRepo.save(musics);
	}

	
}
