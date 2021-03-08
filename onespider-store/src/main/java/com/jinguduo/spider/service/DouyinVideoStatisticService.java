package com.jinguduo.spider.service;

import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.common.constant.DouyinThreshold;
import com.jinguduo.spider.data.table.DouyinVideoStatistic;
import com.jinguduo.spider.db.repo.DouyinVideoStatisticRepo;
import com.jinguduo.spider.store.DouyinVideoStatisticCsvFileWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DouyinVideoStatisticService {

	@Autowired
	private DouyinVideoStatisticRepo douyinVideoStatisticRepo;
	
	@Autowired
    private DouyinVideoStatisticCsvFileWriter douyinVideoStatisticCsvFileWriter;
	
	public Iterable<DouyinVideoStatistic> save(List<DouyinVideoStatistic> statistics) {
		if (statistics == null || statistics.isEmpty()) {
			return Lists.emptyList();
		}
		// save to file
		try {
			douyinVideoStatisticCsvFileWriter.write(statistics);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		// save to db
		for (DouyinVideoStatistic s : statistics) {
			if (s.getDiggCount() >= DouyinThreshold.VIDEO_DIGG_COUNT) {
				douyinVideoStatisticRepo.save(s);
			}
		}
		return statistics;
	}
	
}
