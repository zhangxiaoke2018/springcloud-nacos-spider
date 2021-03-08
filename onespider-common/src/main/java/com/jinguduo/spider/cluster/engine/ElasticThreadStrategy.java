package com.jinguduo.spider.cluster.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jinguduo.spider.common.thread.ElasticCountableThreadPool;
import com.jinguduo.spider.webmagic.thread.CountableThreadPool;

public class ElasticThreadStrategy implements ThreadStrategy {
	
	private int threadNum;
	private String threadName;
	private ElasticCountableThreadPool elasticThreadPool;
	
	public ElasticThreadStrategy() {
		this(1, null);
	}
	
	public ElasticThreadStrategy(String threadName) {
		this(1, threadName);
	}
	
	public ElasticThreadStrategy(int initThreadNum, String threadName) {
		this.threadNum = initThreadNum;
		this.threadName = threadName;
	}

	@Override
	public CountableThreadPool createThreadPool() {
		if (threadNum <= 0) {
			throw new IllegalArgumentException("threadNum should be more than one!");
		}
		if (!StringUtils.hasText(threadName)) {
			threadName = RandomStringUtils.randomAlphabetic(12);
		}
		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(threadName + "-%d").build();
		ExecutorService executor = Executors.newCachedThreadPool(factory);
		elasticThreadPool = new ElasticCountableThreadPool(threadNum, executor);
		return elasticThreadPool;
	}

	@Override
	public int resize(int threadNum) {
		if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
		this.threadNum = threadNum;
		elasticThreadPool.setThreadNum(threadNum);
		return threadNum;
	}

}
