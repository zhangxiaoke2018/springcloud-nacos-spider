package com.jinguduo.spider.common.thread;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class TimeDelay {
	
	private static int threadInitNumber = 1;
    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }
    
    private final static DelayQueue<DelayedTask> queue = new DelayQueue<>();
    
    public static void execute(Runnable task, long delay, TimeUnit unit) {
        queue.add(new DelayedTask(task, delay, unit));
    }
    
    private final static Thread executor = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final DelayedTask delayedTask = queue.take();
                    
                    if (delayedTask != null) {
                       Thread thread = new Thread(delayedTask.task, "TimeDelayThread-" + nextThreadNum());
                       thread.start();
                    }
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }, "TimeDelay");
    
    static {
        executor.start();
    }
    
    
    static class DelayedTask implements Delayed {
        private Runnable task;
        private long startTime;  // millis
        
        public DelayedTask(Runnable task, long delay, TimeUnit timeUnit) {
            this.task = task;
            this.startTime = System.currentTimeMillis() + timeUnit.toMillis(delay);
        }

        @Override
        public int compareTo(Delayed o) {
            int r = 0;
            long delay = startTime - System.currentTimeMillis();
            if (delay < o.getDelay(TimeUnit.MILLISECONDS)) {
                r = -1;
            } else if (delay > o.getDelay(TimeUnit.MILLISECONDS)) {
                r = 1;
            }
            return r;
        }

        @Override
        public long getDelay(TimeUnit unit) {
        	long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }
    }
}
