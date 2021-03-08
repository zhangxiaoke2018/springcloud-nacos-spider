package com.jinguduo.spider.cluster.downloader.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;

import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.common.util.OrderedComponent;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

/**
 * DownloaderListener配置类
 * 
 * <p>
 *   DownloaderListener按序（Order）从小到大排列 <br>
 *   Integer.MIN_VALUE <-- DownloaderListener --> Integer.MAX_VALUE <br>
 *      Spider side    <-- DownloaderListener -->  Downloader side <br>
 * <p>
 *   processRequest和processError方法按正序迭代处理DownloaderListener<br>
 *   processError方法按逆序迭代处理DownloaderListener<br>
 *
 */
class DownloaderListenerConfig {
    
    private List<OrderedComponent<DownloaderListener>> listeners = new ArrayList<>();
    
    public DownloaderListenerConfig() {
    }
    
    public DownloaderListenerConfig(DownloaderListenerConfig conf) {
        listeners.addAll(conf.listeners);
    }

    public void addListener(DownloaderListener downloaderListener) {
        this.addListener(downloaderListener, 0);
    }
    
    public void addListener(DownloaderListener downloaderListener, final int order) {
        if (downloaderListener == null) {
            throw new IllegalArgumentException("The DownloaderListener is null.");
        }
        addListener(new OrderedComponent<>(downloaderListener, order));
    }
    
    public void addListeners(List<OrderedComponent<DownloaderListener>> listeners) {
        for (OrderedComponent<DownloaderListener> listener : listeners) {
            addListener(listener);
        }
    }
    
    private void addListener(OrderedComponent<DownloaderListener> listenerComponent) {
        if (listeners.isEmpty()) {
            listeners.add(listenerComponent);
        } else {
            for (int i = 0; i < listeners.size(); i++) {
                OrderedComponent<DownloaderListener> old = listeners.get(i);
                if (old.getOrder() >= listenerComponent.getOrder()) {
                    listeners.add(i, listenerComponent);
                    break;
                }
            }
        }
    }

    public void processRequest(HttpClientRequestContext requestContext, Request req, Task task) {
        if (listeners != null && !listeners.isEmpty()) {
            // 正序
            for (OrderedComponent<DownloaderListener> component : listeners) {
                component.getComponent().onRequest(requestContext, req, task);
            }
        }
    }

    public void processResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
        if (listeners != null && !listeners.isEmpty()) {
            // 逆序
            for (int i = listeners.size(); i > 0; ) {
                listeners.get(--i).getComponent().onResponse(requestContext, req, resp, task);
            }
        }
    }

    public void processError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
        if (listeners != null && !listeners.isEmpty()) {
            // 正序
            for (OrderedComponent<DownloaderListener> component : listeners) {
                component.getComponent().onError(requestContext, req, e, task);
            }
        }
    }
}