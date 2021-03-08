package com.jinguduo.spider.common.type;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SequenceCounterQuota implements Quota {
    
    private Map<String, Counter> table = new ConcurrentHashMap<>();
    
    private final int limit;
    
    public SequenceCounterQuota(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean isAboved(String key) {
        Counter counter = table.get(key);
        if (counter == null) {
            synchronized(this) {
                counter = table.get(key);
                if (counter == null) {
                    counter = new Counter(this.limit);
                    table.put(key, counter);
                }
            }
        }
        return counter.shoot();
    }
    
    @Override
    public void reset(String key) {
        Counter counter = table.get(key);
        if (counter != null) {
            counter.reset();
        }
    }

    class Counter {
        volatile AtomicInteger value = new AtomicInteger(0);
        final int limit;
        
        Counter(final int limit) {
            this.limit = limit;
        }
        
        synchronized boolean shoot() {
            return value.getAndIncrement() < limit;
        }
        
        synchronized void reset() {
            value.set(0);
        }
    }
}
