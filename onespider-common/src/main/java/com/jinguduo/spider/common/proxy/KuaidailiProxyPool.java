package com.jinguduo.spider.common.proxy;

import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 快代理公开代理池
 * 
 */
@CommonsLog
public class KuaidailiProxyPool implements ProxyPool {

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    private BlockingQueue<Proxy> queue = new LinkedBlockingQueue<>();

    @Override
    public synchronized void addProxy(Proxy... proxies) {
        if (proxies == null || proxies.length == 0) {
            return;
        }
        writeLock.lock();
        try {
        	for (Proxy proxy : proxies) {
        		queue.offer(proxy);
        	}
        	if (log.isDebugEnabled()) {
        		log.debug("kuaidaili proxy queue size: " + queue.size());
        	}
		} finally {
			writeLock.unlock();
		}
    }

    private final static long WAITTING_MILLS = 200L;
    public Proxy getProxy() {
    	Proxy proxy = null;
    	readLock.lock();
    	try {
    		proxy = queue.poll(WAITTING_MILLS, TimeUnit.MILLISECONDS);
    		if (proxy != null) {
    			queue.offer(proxy);  // 立即放回，不限制重复使用
			}
    	} catch (InterruptedException e) {
    		log.error(e.getMessage(), e);
    	} finally {
    		readLock.unlock();
    	}
    	return proxy;
    }

    @Override
    public void returnProxy(Proxy proxy, Page page) {
    	// nothing
    }

    @Override
    public void remove(Proxy proxy) {
    	writeLock.lock();
        try {
			queue.removeIf(p -> p.getHost().equals(proxy.getHost()));
        } finally {
			writeLock.unlock();
		}
    }

    public void clear(){
		writeLock.lock();
		try {
			queue.clear();
		} finally {
			writeLock.unlock();
		}
	}

    @Override
    public int getPoolSize() {
        return queue.size();
    }
    
    public boolean contains(Proxy proxy) {
    	return queue.contains(proxy);
    }
}
