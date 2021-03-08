package com.jinguduo.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.DouyinMusicBillboard;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DouyinMusicBillboardService {
	
	@Autowired
	private DouyinMusicBillboardRepo douyinMusicBillboardRepo;

	public boolean save(List<DouyinMusicBillboard> billboard) {
		boolean r = true;
		for (DouyinMusicBillboard music : billboard) {
			DouyinMusicBillboard o = douyinMusicBillboardRepo.findOneByOrdinalAndActiveTime(
					music.getOrdinal(), music.getActiveTime());
			if (o != null) {
				DbEntityHelper.copy(music, o, new String[] {"id"});
				music = o;
			}
			try {
				douyinMusicBillboardRepo.save(music);
			} catch (Exception e) {
				// ignore
				r = false;
				log.error(e.getMessage(), e);
			}
		}
		return r;
	}
}
