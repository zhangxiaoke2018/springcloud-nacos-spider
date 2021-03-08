package com.jinguduo.spider.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.constant.WorkerCommand;

@Component
public class WorkerTracker {
	
	@Autowired
	private WorkerManager workerManager;

	public WorkerCommand hearbeat(String hostname, String uuid, String domain, SpiderStatus status, WorkerCommand command) {
		return workerManager.heartbeat(hostname, uuid, domain, status, command);
	}

	
}
