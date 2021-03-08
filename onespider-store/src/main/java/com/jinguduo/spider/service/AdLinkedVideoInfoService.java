package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.db.repo.AdLinkedVideoInfosRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public class AdLinkedVideoInfoService {

	@Autowired
	private AdLinkedVideoInfosRepo adLinkedVideoInfosRepo;

	public AdLinkedVideoInfos insert(AdLinkedVideoInfos infos) {
		assert infos != null;

		if (StringUtils.isBlank(infos.getCode())) {
			return null;
		}

		AdLinkedVideoInfos oldItem = adLinkedVideoInfosRepo.findByCodeAndPlatformId(infos.getCode(),
				infos.getPlatformId());
		if (oldItem != null) {
			if (StringUtils.isNoneBlank(infos.getTitle()))
				oldItem.setTitle(infos.getTitle());
			
			if (StringUtils.isNoneBlank(infos.getCategory()))
				oldItem.setCategory(infos.getCategory());

			if (StringUtils.isNoneBlank(infos.getPageUrl()))
				oldItem.setPageUrl(infos.getPageUrl());
			
			if (infos.getSeconds()!=null&&infos.getSeconds()>0)
				oldItem.setSeconds(infos.getSeconds());

			if (infos.getPlayCount() != null && infos.getPlayCount() >0)
				oldItem.setPlayCount(infos.getPlayCount());

			oldItem.setUpdatedAt(infos.getUpdatedAt());
			

			infos = oldItem;
		}
		return adLinkedVideoInfosRepo.save(infos);
	}
}
