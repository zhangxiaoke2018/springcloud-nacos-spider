package com.jinguduo.spider.cluster.spider;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.config.CookieSpecs;

import com.jinguduo.spider.cluster.downloader.handler.PageHandler;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.proxy.ProxyPool;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper = true)
public class Site extends com.jinguduo.spider.webmagic.Site {

    private List<SpiderListener> spiderListeners;
    
    private PageHandler pageHandler;
    
    private ProxyPool proxyPool;
    
    private String cookieSpecs = CookieSpecs.STANDARD;
    
    private Integer frequency = FrequencyConstant.DEFAULT;
    
    private int retryDelayTime = 10;  // seconds
    
    private final static int DEFAULT_SLEEP_TIME = 0; 
    
    public Site() {
		super.setSleepTime(DEFAULT_SLEEP_TIME);
	}

    public void addSpiderListener(SpiderListener listener) {
        if (spiderListeners == null) {
            spiderListeners = new ArrayList<>();
        }
        spiderListeners.add(listener);
    }
    
    public void setProxyPool(ProxyPool proxyPool) {
        this.proxyPool = proxyPool;
    }
    
    public Proxy getProxy() {
        if (proxyPool != null) {
            return proxyPool.getProxy();
        }
        return null;
    }

    public void returnProxy(Proxy proxy, Page page) {
        proxyPool.returnProxy(proxy, page);
    }
}
