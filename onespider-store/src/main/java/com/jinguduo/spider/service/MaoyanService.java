package com.jinguduo.spider.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jinguduo.spider.data.table.MaoyanActor;
import com.jinguduo.spider.db.repo.MaoyanActorRepo;

@Service
public class MaoyanService {
	@Autowired
	private MaoyanActorRepo maoyanActorRepo;

	public void insertOrUpdate(MaoyanActor newRecord) {
		if (StringUtils.isEmpty(newRecord.getCode()) || StringUtils.isEmpty(newRecord.getName()) || newRecord.getName().length() > 8) {
			return;
		}

		MaoyanActor oldRecord = maoyanActorRepo.findByCode(newRecord.getCode());
		if (null != oldRecord) {
			newRecord.setId(oldRecord.getId());
		}

		maoyanActorRepo.save(newRecord);
	}

}
