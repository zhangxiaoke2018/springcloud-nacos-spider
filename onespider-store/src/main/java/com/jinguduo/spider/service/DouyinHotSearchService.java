package com.jinguduo.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.DouyinHotSearch;
import com.jinguduo.spider.db.repo.DouyinHotSearchRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DouyinHotSearchService {
	
	@Autowired
	private DouyinHotSearchRepo douyinHotSearchRepo;

	public boolean save(List<DouyinHotSearch> searches) {
		boolean r = true;
		for (DouyinHotSearch search : searches) {
			DouyinHotSearch o = douyinHotSearchRepo.findOneByOrdinalAndActiveTime(
					search.getOrdinal(), search.getActiveTime());
			
			if (o != null) {
				DbEntityHelper.copy(search, o, new String[] {"id"});
				search = o;
			}
			try {
				douyinHotSearchRepo.save(search);
			} catch (Exception e) {
				// ignore
				r = false;
				log.error(e.getMessage(), e);
			}
		}
		return r;
	}

}
