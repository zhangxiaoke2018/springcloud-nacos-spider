package com.jinguduo.spider.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.jinguduo.spider.webmagic.thread.CountableThreadPool;

/**
 * 可动态调整线程数量的线程池
 *
 */
public class ElasticCountableThreadPool extends CountableThreadPool {

	private int threadNum;

    private AtomicInteger threadAlive = new AtomicInteger();

    private ReentrantLock reentrantLock = new ReentrantLock();

    private Condition condition = reentrantLock.newCondition();

    public ElasticCountableThreadPool(int threadNum) {
        this(threadNum, Executors.newCachedThreadPool());
    }

    public ElasticCountableThreadPool(int threadNum, ExecutorService executorService) {
        super(threadNum, executorService);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public int getThreadAlive() {
        return threadAlive.get();
    }

    public int getThreadNum() {
        return threadNum;
    }
    
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    private ExecutorService executorService;

    public void execute(final Runnable runnable) {
    	reentrantLock.lock();
    	try {
    		while (threadAlive.get() >= threadNum) {
    			try {
    				condition.await();
    			} catch (InterruptedException e) {
    				// ignore
    			}
    		}
    		threadAlive.incrementAndGet();
    		executorService.execute(new Runnable() {
    			@Override
    			public void run() {
    				try {
    					runnable.run();
    				} finally {
    					reentrantLock.lock();
    					try {
    						threadAlive.decrementAndGet();
    						condition.signal();
    					} finally {
    						reentrantLock.unlock();
    					}
    				}
    			}
    		});
    	} finally {
    		reentrantLock.unlock();
    	}
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
