package com.jinguduo.spider.cluster.spider.listener;

import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

/**
 * 改进的爬虫监听接口
 */
public interface SpiderListener {

    void onStart(Task task);
    
    void onRequest(Request request, Task task);
    
    void onResponse(Request request, Page page, Task task);
    
    void onError(Request request, Exception e, Task task);
    
    void onExit(Task task);
    
    default public int getOrder() {
        return 0;
    }
}
