package com.jinguduo.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.DouyinAuthor;
import com.jinguduo.spider.db.repo.DouyinAuthorRepo;

@Service
public class DouyinAuthorService {

	@Autowired
	private DouyinAuthorRepo douyinAuthorRepo;
	
	public Iterable<DouyinAuthor> save(List<DouyinAuthor> authors) {
		if (authors == null || authors.isEmpty()) {
			return null;
		}
		return douyinAuthorRepo.save(authors);
	}
}
