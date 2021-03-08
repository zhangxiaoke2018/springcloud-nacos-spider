package com.jinguduo.spider.cluster.spider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jinguduo.spider.cluster.downloader.handler.PageHandler;
import com.jinguduo.spider.cluster.downloader.listener.DownloaderListener;
import com.jinguduo.spider.cluster.downloader.listener.DownloaderListenerManager;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.util.OrderedComponent;
import com.jinguduo.spider.common.util.SpringAgent;

public class SiteBuilder {
    
    private List<OrderedComponent<DownloaderListener>> downloaderListeners = new ArrayList<>();

    private Site site = new Site();

    public static SiteBuilder builder() {
        return new SiteBuilder();
    }

    public SiteBuilder addDownloaderListener(DownloaderListener listener) {
        this.addDownloaderListener(listener, 0);
        return this;
    }

    public SiteBuilder addDownloaderListener(DownloaderListener listener, int order) {
    	SpringAgent.autowireBean(listener);
        this.downloaderListeners.add(new OrderedComponent<>(listener, order));
        return this;
    }
    
    public SiteBuilder addSpiderListener(SpiderListener listener) {
        SpringAgent.autowireBean(listener);
        site.addSpiderListener(listener);
        return this;
    }
    
    public SiteBuilder setPageHandler(PageHandler handler) {
        site.setPageHandler(handler);
        return this;
    }
    
    public Site build() {
        String domain = site.getDomain();
        if (domain != null) {
            DownloaderListenerManager.addDownloaderListener(domain, downloaderListeners);
        }
        return site;
    }

    public SiteBuilder setDomain(String domain) {
        site.setDomain(domain);
        return this;
    }

    public SiteBuilder setCharset(String charset) {
        site.setCharset(charset);
        return this;
    }

    public SiteBuilder setAcceptStatCode(Set<Integer> acceptStatCode) {
        site.setAcceptStatCode(acceptStatCode);
        return this;
    }
    
    public SiteBuilder setUserAgent(String ua) {
        site.setUserAgent(ua);
        return this;
    }
    
    public SiteBuilder addHeader(String key, String value) {
        site.addHeader(key, value);
        return this;
    }
    
    public SiteBuilder addCookie(String name, String value) {
        site.addCookie(name, value);
        return this;
    }
    
    public SiteBuilder addCookie(String domain, String name, String value) {
        site.addCookie(domain, name, value);
        return this;
    }
    
    public SiteBuilder setSleepTime(int sleepTime) {
        site.setSleepTime(sleepTime);
        return this;
    }
    
    public SiteBuilder setCookieSpecs(String cookieSpecs) {
        site.setCookieSpecs(cookieSpecs);
        return this;
    }
}
