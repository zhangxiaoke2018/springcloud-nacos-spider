package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.db.repo.FictionIncomeLogsRepo;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FictionIncomeLogsService {

	@Autowired
	private FictionIncomeLogsRepo fictionIncomeLogsRepo;

	public FictionIncomeLogs insert(FictionIncomeLogs fictionIncomeLogs) {
		if(StringUtils.isEmpty(fictionIncomeLogs.getCode())||fictionIncomeLogs.getIncomeId()==null)return null;
		
		FictionIncomeLogs oldLog = fictionIncomeLogsRepo.findByIncomeIdAndCodeAndDay(fictionIncomeLogs.getIncomeId(), fictionIncomeLogs.getCode(),fictionIncomeLogs.getDay());
		
		if(oldLog!=null){
			if(fictionIncomeLogs.getIncomeNum()==null) {
				return oldLog;//如果新抓的数据为空跳过
			}
			fictionIncomeLogs.setId(oldLog.getId());
		}
		
		return fictionIncomeLogsRepo.save(fictionIncomeLogs);
	}

}
