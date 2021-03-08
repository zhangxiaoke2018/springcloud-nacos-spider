package com.jinguduo.spider.service;

import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.DouyinDevice;
import com.jinguduo.spider.db.repo.DouyinDeviceRepo;

@Service
public class DouyinDeviceService {
	
	@Autowired
	private DouyinDeviceRepo douyinDeviceRepo;

	public void save(List<DouyinDevice> devices) {
		douyinDeviceRepo.save(devices);
	}

	public Iterable<DouyinDevice> findByRandomSorted(Integer size) {
        Pageable pr = new PageRequest(0, size);
        Page<DouyinDevice> r = douyinDeviceRepo.findByRandomSorted(pr);
        if (r != null) {
        	return r.getContent();
		}
        return Lists.emptyList();
	}

}
