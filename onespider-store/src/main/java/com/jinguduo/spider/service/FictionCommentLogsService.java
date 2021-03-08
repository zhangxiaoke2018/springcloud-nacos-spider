package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.db.repo.FictionCommentLogsRepo;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FictionCommentLogsService {

	@Autowired
	private FictionCommentLogsRepo fictionCommentLogsRepo;

	public FictionCommentLogs insert(FictionCommentLogs fictionCommentLogs) {
		if (StringUtils.isEmpty(fictionCommentLogs.getCode()) || fictionCommentLogs.getPlatformId() == null)
			return null;

		FictionCommentLogs oldLog = fictionCommentLogsRepo.findByCodeAndPlatformIdAndDay(fictionCommentLogs.getCode(),
				fictionCommentLogs.getPlatformId(), fictionCommentLogs.getDay());

		if (oldLog != null) {
			if(fictionCommentLogs.getCommentCount()==null) {
				return oldLog;//如果新抓的数据为空跳过
			}
			fictionCommentLogs.setId(oldLog.getId());
		}

		return fictionCommentLogsRepo.save(fictionCommentLogs);
	}

}
