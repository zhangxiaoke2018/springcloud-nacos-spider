package com.jinguduo.spider.common.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 在指定时间内完成提交任务，超时取消任务。
 * 
 */
public class Timeout<T> {
    
    private static ExecutorService executor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("Timeout-%d").build());

    public static void execute(Runnable task, long timeout, TimeUnit unit) 
            throws InterruptedException, ExecutionException, TimeoutException {
        
        Future<?> future = executor.submit(task);
        future.get(timeout, unit);
    }

}
