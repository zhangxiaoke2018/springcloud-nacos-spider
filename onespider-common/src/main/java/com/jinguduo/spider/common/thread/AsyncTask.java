package com.jinguduo.spider.common.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 异步任务工具类
 * 
 */
public class AsyncTask {
    
    private ExecutorService executor;
    
    public AsyncTask() {
        this(1);
    }
    
    public AsyncTask(int concurrency) {
        executor = Executors.newFixedThreadPool(
                concurrency,
                new ThreadFactoryBuilder().setNameFormat("AsyncTask-%d").build());
    }
    
    public AsyncTask(int concurrency, String name) {
        executor = Executors.newFixedThreadPool(
                concurrency,
                new ThreadFactoryBuilder().setNameFormat(name + "-%d").build());
    }

    public <T> Future<T> execute(Callable<T> task) {
        return executor.submit(task);
    }
    
    public Future<?> execute(Runnable task) {
        return executor.submit(task);
    }

    public void shutdown() {
    	executor.shutdown();
    }
}
