package com.jinguduo.spider.spider.douban;

import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.cluster.downloader.listener.DownloaderListener;
import com.jinguduo.spider.common.type.ConcurrentHashMultiMap;
import com.jinguduo.spider.common.type.HashMultiMap;
import com.jinguduo.spider.common.type.Quota;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.slf4j.Slf4j;

/**
 * 保存并重用Cookie
 */
@Slf4j
public class DoubanRandomCookieDownloaderListener implements DownloaderListener {

    private Quota quota = null;
    // 需要检查的异常HttpStatuCode
    private Set<Integer> abnormalStatusCode = null;
    //
    private int bound = 100;
    private int prob = 0; // probability = 0%
    private Random random = new Random();
    
    private Holder holder = new Holder();
    
    @Override
    public void onRequest(HttpClientRequestContext requestContext, Request req, Task task) {
        String domain = task.getSite().getDomain();
        // set Cookie
        CookieStore cookieStore = requestContext.getHttpClientContext().getCookieStore();
        if (cookieStore == null
                || CollectionUtils.isEmpty(cookieStore.getCookies())
                || prob == 100
                || (prob > 0 && random.nextInt(bound) < prob)) {
            resetCookie(requestContext, domain);
        }
    }

    @Override
    public void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
        String domain = task.getSite().getDomain();
        
        final int statusCode = resp.getStatusLine().getStatusCode();
        if (abnormalStatusCode != null && abnormalStatusCode.contains(statusCode)) {
            removeCookie(requestContext, domain);
        }
        // 保存Cookie
        returnCookie(requestContext, domain);
    }
    
    @Override
    public void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
        String domain = task.getSite().getDomain();
        // set cookies
        if ((quota != null && quota.isAboved(domain))) {
            removeCookie(requestContext, domain);
        }
        returnCookie(requestContext, domain);
    }

    static class Holder {
        // domain -> value
        private final static HashMultiMap<String, CookieStore> cookies = new ConcurrentHashMultiMap<>();
        
        void add(String domain, CookieStore cookie) {
            cookies.put(domain, cookie);
        }
        
        CookieStore pop(String domain) {
            return cookies.pop(domain);
        }
    }
    
    private void removeCookie(HttpClientRequestContext requestContext, String domain) {
        requestContext.getHttpClientContext().setCookieStore(new BasicCookieStore());
    }
    
    private void resetCookie(HttpClientRequestContext requestContext, String domain) {
        HttpClientContext httpClientContext = requestContext.getHttpClientContext();
        CookieStore cookieStore = holder.pop(domain);
        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
            setCookie(cookieStore, "bid", RandomStringUtils.randomAlphanumeric(11));
            setCookie(cookieStore, "ll", "0");
        }
        httpClientContext.setCookieStore(cookieStore);
    }

    private final static String DOMAIN = "douban.com";
    private final static long ONE_YEAR = TimeUnit.DAYS.toMillis(365);
    
    private CookieStore setCookie(CookieStore cookieStore, String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(DOMAIN);
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, DOMAIN);
        cookie.setExpiryDate(new Date(System.currentTimeMillis() + ONE_YEAR));
        cookie.setPath("/");
        cookie.setVersion(0);
        cookie.setSecure(false);
        cookieStore.addCookie(cookie);
        return cookieStore;
    }

    private void returnCookie(HttpClientRequestContext requestContext, String domain) {
        // 
        CookieStore cookieStore = requestContext.getHttpClientContext().getCookieStore();
        if (cookieStore != null && !CollectionUtils.isEmpty(cookieStore.getCookies())) {
            holder.add(domain, cookieStore);
        }
    }

    public DoubanRandomCookieDownloaderListener setQuota(Quota quota) {
        this.quota = quota;
        return this;
    }
    
    public DoubanRandomCookieDownloaderListener addAbnormalStatusCode(int... codes) {
        if (abnormalStatusCode == null) {
            abnormalStatusCode = Sets.newConcurrentHashSet();
        }
        for (int code : codes) {
            abnormalStatusCode.add(code);
        }
        return this;
    }
    
    public DoubanRandomCookieDownloaderListener setProbability(double probability) {
        this.prob = (int)(probability * bound);
        return this;
    }
}
