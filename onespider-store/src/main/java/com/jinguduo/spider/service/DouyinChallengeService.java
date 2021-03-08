package com.jinguduo.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.DouyinChallenge;
import com.jinguduo.spider.db.repo.DouyinChallengeRepo;

@Service
public class DouyinChallengeService {

	@Autowired
	private DouyinChallengeRepo douyinChallengeRepo;

	public Iterable<DouyinChallenge> save(List<DouyinChallenge> challenges) {
		if (challenges == null || challenges.isEmpty()) {
			return null;
		}
		return douyinChallengeRepo.save(challenges);
	}
}
