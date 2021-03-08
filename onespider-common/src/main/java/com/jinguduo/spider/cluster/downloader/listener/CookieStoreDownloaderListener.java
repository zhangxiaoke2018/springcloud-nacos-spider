package com.jinguduo.spider.cluster.downloader.listener;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.common.type.ConcurrentHashMultiMap;
import com.jinguduo.spider.common.type.HashMultiMap;
import com.jinguduo.spider.common.type.Quota;
import com.jinguduo.spider.common.util.CookieMapper;
import com.jinguduo.spider.data.loader.CookieStringStoreLoader;
import com.jinguduo.spider.data.table.CookieString;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.slf4j.Slf4j;

/**
 * 保存并重用Cookie
 * 
 * XXX: 使用这个监听器要小心，可能会把DB表存爆造成阻塞!!!
 * 
 */
@Slf4j
public class CookieStoreDownloaderListener implements DownloaderListener {
    
    @Autowired
    private CookieStringStoreLoader cookieStringStoreLoader;

    private Quota quota = null;
    // 需要检查的异常HttpStatuCode
    private Set<Integer> abnormalStatusCode = null;
    //
    private int bound = 100;
    private int prob = 0; // probability = 0%
    private Random random = new Random();
    
    private volatile boolean mustBeLoaded = true;
    private volatile long lastestReloadTime = 0;
    private long reloadTimeMills = TimeUnit.MINUTES.toMillis(5);
    
    private Holder holder = new Holder();
    
    @Override
    public void onRequest(HttpClientRequestContext requestContext, Request req, Task task) {
        String domain = task.getSite().getDomain();
        // load cookie on first
        if (needToReload()) {
            reloadCookies(domain);
        }
        // set Cookie
        HttpClientContext httpClientContext = requestContext.getHttpClientContext();
        CookieStore cookieStore = httpClientContext.getCookieStore();
        if (cookieStore == null 
                || cookieStore.getCookies() == null
                || cookieStore.getCookies().isEmpty()) {
            
            resetCookie(requestContext, domain);
        } else if (prob == 100
                || (prob > 0 && random.nextInt(bound) < prob)) {
            removeCookie(requestContext, domain);
        }
    }

    @Override
    public void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
        String domain = task.getSite().getDomain();
        
        // 保存服务端Set-Cookie
        saveSetCookie(requestContext, resp, domain);
        
        final int statusCode = resp.getStatusLine().getStatusCode();
        if (abnormalStatusCode != null && abnormalStatusCode.contains(statusCode)) {
            resetCookie(requestContext, domain);
        }
    }
    
    @Override
    public void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
        String domain = task.getSite().getDomain();
        // set cookies
        if ((quota != null && quota.isAboved(domain))) {
            resetCookie(requestContext, domain);
        }
    }
    
    static class Holder {
        // domain -> value
        private final static HashMultiMap<String, CookieString> cookies = new ConcurrentHashMultiMap<>();
        
        void add(CookieString cookie) {
            cookies.put(cookie.getDomain(), cookie);
        }
        
        CookieString pop(String domain) {
            return cookies.pop(domain);
        }
    }
    
    private void removeCookie(HttpClientRequestContext requestContext, String domain) {
        HttpClientContext httpClientContext = requestContext.getHttpClientContext();
        httpClientContext.setCookieStore(new BasicCookieStore());
    }
    
    private void resetCookie(HttpClientRequestContext requestContext, String domain) {
        HttpClientContext httpClientContext = requestContext.getHttpClientContext();
        CookieStore cookieStore = new BasicCookieStore();
        CookieString cookieString = holder.pop(domain);
        if (cookieString != null) {
            cookieStore = CookieMapper.readCookieStore(cookieString, cookieStore);
        }
        httpClientContext.setCookieStore(cookieStore);
    }

    private void saveSetCookie(HttpClientRequestContext requestContext, HttpResponse resp, String domain) {
        // 
        CookieStore cookieStore = requestContext.getHttpClientContext().getCookieStore();
        CookieString cookieString = CookieMapper.writeCookieString(cookieStore, domain);
        if (cookieString != null) {
            holder.add(cookieString);
        }
        
        // 保存服务端Set-Cookie
        Header[] headers = resp.getHeaders("Set-Cookie");
        if (headers != null && headers.length > 0
                && cookieString != null
                && cookieStringStoreLoader != null) {
            cookieStringStoreLoader.save(cookieString);
        }
    }

    private void reloadCookies(String domain) {
        if (cookieStringStoreLoader == null) {
            log.warn("The CookieStringStoreLoader is null.");
            return;
        }
        mustBeLoaded = false;
        List<CookieString> cookies = cookieStringStoreLoader.load(domain);
        if (cookies != null && !cookies.isEmpty()) {
            Collections.shuffle(cookies);
            for (CookieString cookie : cookies) {
                holder.add(cookie);
            }
        }
    }
    
    private boolean needToReload() {
        if (!mustBeLoaded || isTimeToReload()) {
            synchronized (this) {
                if (!mustBeLoaded) {
                    mustBeLoaded = true;
                    lastestReloadTime = System.currentTimeMillis();
                }
            }
        }
        return mustBeLoaded;
    }

    private boolean isTimeToReload() {
        return (lastestReloadTime + reloadTimeMills) < System.currentTimeMillis();
    }
    
    public CookieStoreDownloaderListener setCookiesStoreLoader(CookieStringStoreLoader cookieStringStoreLoader) {
        this.cookieStringStoreLoader = cookieStringStoreLoader;
        return this;
    }

    public CookieStoreDownloaderListener setQuota(Quota quota) {
        this.quota = quota;
        return this;
    }
    
    public CookieStoreDownloaderListener addAbnormalStatusCode(int... codes) {
        if (abnormalStatusCode == null) {
            abnormalStatusCode = Sets.newConcurrentHashSet();
        }
        for (int code : codes) {
            abnormalStatusCode.add(code);
        }
        return this;
    }
    
    public CookieStoreDownloaderListener setProbability(double probability) {
        this.prob = (int)(probability * bound);
        return this;
    }
}
