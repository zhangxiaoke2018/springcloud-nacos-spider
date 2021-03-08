package com.jinguduo.spider.cluster.downloader.handler;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.data.table.Proxy;

public class WebMagicPageHandler implements PageHandler {

    @Override
    public byte[] getContent(String charset, HttpResponse httpResponse) throws IOException {
        return IOUtils.toByteArray(httpResponse.getEntity().getContent());
    }
    
    protected Proxy getProxy(HttpClientRequestContext requestContext) {
        return requestContext.getProxy();
    }
}
