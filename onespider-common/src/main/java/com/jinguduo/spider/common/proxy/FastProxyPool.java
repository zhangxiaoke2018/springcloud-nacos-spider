package com.jinguduo.spider.common.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 代理服务器池
 * 
 * <p>增加：支持Socks代理
 * <p>维护一份代理服务器索引，不重用已标记为失败的代理IP
 * 
 */
@CommonsLog
public class FastProxyPool implements ProxyPool {

    private BlockingDeque<Proxy> queue = new LinkedBlockingDeque<>();

    // Host -> Proxy
    private Map<String, Proxy> index = new ConcurrentHashMap<>();
    
    @Override
    public void addProxy(Proxy... proxies) {
        for (Proxy proxy : proxies) {
            if (index.get(proxy.getHost()) != null) {
                continue;
            }
            addProxyImpl(proxy);
        }
        if (log.isDebugEnabled()) {
            log.debug("proxy queue size: " + queue.size());
        }
    }
    
    private void addProxyImpl(Proxy proxy) {
        index.put(proxy.getHost(), proxy);
        queue.offerFirst(proxy);
    }

    public Proxy getProxy() {
        Proxy proxy = popProxyFromQueue();
        return proxy;
    }

    private final static long WAITTING_MILLS = 200L;
    
    private Proxy popProxyFromQueue() {
        Proxy proxy = null;
        try {
            proxy = queue.poll(WAITTING_MILLS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return proxy;
    }

    @Override
    public void returnProxy(Proxy proxy, Page page) {
        Proxy p = index.get(proxy.getHost());
        if (p == null) {
            p = proxy;
        }
        if (page != null && page.isDownloadSuccess()) {
            p.setState(ProxyState.Availabled);;
            // cycle using
            queue.offer(p);
            index.put(proxy.getHost(), p);
        } else {
            // maybe broken
        	p.setState(ProxyState.Broken);
        }
    }
    
    @Override
    public void remove(Proxy proxy) {
        queue.removeIf(p -> p.getHost().equals(proxy.getHost()));
        index.remove(proxy.getHost());
    }
    
    public List<Proxy> findAllWithBroken() {
        List<Proxy> r = new ArrayList<>();
        for (Proxy proxy : index.values()) {
            if (proxy.isBroken()) {
                r.add(proxy);
            }
        }
        return r;
    }

    @Override
    public int getPoolSize() {
        return index.size();
    }

}
