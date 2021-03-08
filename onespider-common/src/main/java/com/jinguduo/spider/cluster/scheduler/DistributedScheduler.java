package com.jinguduo.spider.cluster.scheduler;

import com.jinguduo.spider.cluster.worker.SpiderWorker;
import com.jinguduo.spider.webmagic.scheduler.Scheduler;

public interface DistributedScheduler extends Scheduler {

	void addSpiderWorker(SpiderWorker iSpiderWorker);
	
	void removeSpiderWorker(SpiderWorker iSpiderWorker);
	
}
