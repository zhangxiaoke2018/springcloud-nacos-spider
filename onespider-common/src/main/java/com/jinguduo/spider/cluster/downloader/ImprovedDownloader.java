package com.jinguduo.spider.cluster.downloader;

import org.apache.http.HttpResponse;

import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.downloader.Downloader;

public interface ImprovedDownloader extends Downloader {
    
    void onRequest(HttpClientRequestContext requestContext, Request req, Task task);

    void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task);

    void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task);
}
