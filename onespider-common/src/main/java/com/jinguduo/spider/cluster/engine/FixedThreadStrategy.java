package com.jinguduo.spider.cluster.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jinguduo.spider.webmagic.thread.CountableThreadPool;

public class FixedThreadStrategy implements ThreadStrategy {
	
	private int threadNum;
	
	private String threadName;
	
	public FixedThreadStrategy(int threadNum) {
		this(threadNum, null);
	}
	
	public FixedThreadStrategy(int threadNum, String threadName) {
		this.threadNum = threadNum;
		this.threadName = threadName;
	}

	@Override
	public CountableThreadPool createThreadPool() {
		ThreadFactory factory = new ThreadFactoryBuilder()
			.setNameFormat(threadName + "-%d").build();
		ExecutorService executor = Executors.newFixedThreadPool(threadNum, factory);
		return new CountableThreadPool(threadNum, executor);
	}

	@Override
	public int resize(int threadNum) {
		// ignore
		return this.threadNum;
	}

}
