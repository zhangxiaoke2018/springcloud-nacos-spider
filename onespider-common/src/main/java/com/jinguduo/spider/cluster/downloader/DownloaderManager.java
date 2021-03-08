package com.jinguduo.spider.cluster.downloader;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.jinguduo.spider.cluster.downloader.handler.PageHandler;
import com.jinguduo.spider.cluster.downloader.handler.WebMagicPageHandler;
import com.jinguduo.spider.cluster.downloader.listener.DownloaderListenerManager;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.common.exception.QuickException;
import com.jinguduo.spider.common.metric.MetricFactory;
import com.jinguduo.spider.common.metric.Metrizable;
import com.jinguduo.spider.common.util.HostUtils;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.slf4j.Slf4j;

/**
 * 下载管理
 * <p>覆写WebMagic的Downloader代码
 * <p>把非下载部分代码拆分出来，中间件（Middleware）化。
 *
 */
@Slf4j
public class DownloaderManager implements ImprovedDownloader {
	
    private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();

    private final HttpClientGenerator httpClientGenerator;
    
    private HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();
    
    private final Metrizable requestCounter;
    private final Metrizable errorCounter;
    
    public DownloaderManager() {
    	this(new HttpClientGenerator());
    }
    
    public DownloaderManager(HttpClientGenerator httpClientGenerator) {
    	this.httpClientGenerator = httpClientGenerator;
        requestCounter = MetricFactory.builder()
        		.namespace("onespider_downloader")
                .metricName("downloader_request")
                .addDimension("Host", HostUtils.getHostName())
                .build();
        
        errorCounter = MetricFactory.builder()
        		.namespace("onespider_downloader")
                .metricName("downloader_error")
                .addDimension("Host", HostUtils.getHostName())
                .build();
    }
    
    public void setHttpUriRequestConverter(HttpUriRequestConverter httpUriRequestConverter) {
        this.httpUriRequestConverter = httpUriRequestConverter;
    }

    private CloseableHttpClient getHttpClient(Site site) {
        if (site == null) {
            return httpClientGenerator.getClient(null);
        }
        String domain = site.getDomain();
        CloseableHttpClient httpClient = httpClients.get(domain);
        if (httpClient == null) {
            synchronized (this) {
                httpClient = httpClients.get(domain);
                if (httpClient == null) {
                    httpClient = httpClientGenerator.getClient(site);
                    httpClients.put(domain, httpClient);
                }
            }
        }
        return httpClient;
    }

    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        Site site = (Site)task.getSite();
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = getHttpClient(site);
        Proxy proxy = getProxy(site);
        HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, site, proxy);
        PageHandler pageHandler = site.getPageHandler();
        Page page = Page.fail();
        try {
            onRequest(requestContext, request, task);
            httpResponse = httpClient.execute(requestContext.getHttpUriRequest(), requestContext.getHttpClientContext());
            onResponse(requestContext, request, httpResponse, task);
            if (pageHandler == null) {
                pageHandler = new WebMagicPageHandler();
            }
            String charset = request.getCharset() != null ? request.getCharset() : site.getCharset();
            page = pageHandler.doHandle(request, requestContext, charset, httpResponse, task);
            //onSuccess(request);
            if (log.isDebugEnabled()) {
            	log.debug("downloading page success {}", request.getUrl());
			}
        } catch (IOException e) {
        	if (proxy != null &&
        			(e instanceof ConnectTimeoutException || e instanceof SocketTimeoutException
        					|| e instanceof NoHttpResponseException || e instanceof SocketException)) {
        		// 使用匿名免密代理后，大量这类异常。
        		// 由代理池管理类(ProxyPoolManager)自动处理，这里只输出少量信息用于查询统计
        		log.error(request.getUrl(), new QuickException(e.getMessage()));  // 不输出异常栈
			} else {
				log.error(request.getUrl(), e);
			}
            onError(requestContext, request, e, task);
        } finally {
            if (httpResponse != null) {
                //ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                try {
                	httpResponse.close();
                } catch (IOException e) {
                	log.error(e.getMessage(), e);
                }
            }
            if (proxy != null) {
                returnProxy(proxy, site, page);
            }
        }
        return page;
    }

    private Proxy getProxy(Site site) {
        Proxy proxy = null;
        if ((proxy = site.getProxy()) != null) {
            return proxy;
        }
        return null;
    }
    
    private void returnProxy(Proxy proxy, Site site, Page page) {
        site.returnProxy(proxy, page);
    }

    @Override
    public void setThread(int thread) {
        httpClientGenerator.setPoolSize(thread);
    }

    @Override
    public void onRequest(HttpClientRequestContext requestContext, Request req, Task task) {
        requestCounter.addAndGet(1);
    	DownloaderListenerManager.processRequest(requestContext, req, task);
    }

    @Override
    public void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
        DownloaderListenerManager.processResponse(requestContext, req, resp, task);
    }

    @Override
    public void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
        errorCounter.addAndGet(1);
        DownloaderListenerManager.processError(requestContext, req, e, task);
    }

}
