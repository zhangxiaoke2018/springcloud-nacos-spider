package com.jinguduo.spider.cluster.downloader.handler;

import java.io.IOException;

import org.apache.http.HttpResponse;

import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

public interface ResponseHandler<T> {

    T doHandle(Request request, HttpClientRequestContext requestContext, String charset, HttpResponse httpResponse, Task task) throws IOException;
    
}
