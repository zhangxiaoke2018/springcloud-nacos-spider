package com.jinguduo.spider.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.DouyinVideoDigg;
import com.jinguduo.spider.store.DouyinVideoDiggCsvFileWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DouyinVideoDiggService {
	
	@Autowired
	private DouyinVideoDiggCsvFileWriter douyinVideoDiggCsvFileWriter;

	public boolean save(List<DouyinVideoDigg> diggs) {
		if (diggs == null) {
			return true;
		}
		try {
			douyinVideoDiggCsvFileWriter.write(diggs);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
		return true;
	}

	
}
