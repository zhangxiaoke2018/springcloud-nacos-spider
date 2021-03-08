package com.jinguduo.spider.cluster.engine;

import com.jinguduo.spider.webmagic.thread.CountableThreadPool;

public interface ThreadStrategy {

	CountableThreadPool createThreadPool();

	int resize(int threadNum);

}
