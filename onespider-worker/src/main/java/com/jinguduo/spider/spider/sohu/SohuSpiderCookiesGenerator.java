package com.jinguduo.spider.spider.sohu;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.cluster.downloader.listener.DownloaderListener;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

/**
 * 生成搜狐的反爬虫Cookies
 */
public class SohuSpiderCookiesGenerator implements DownloaderListener {

	@Override
    public void onRequest(HttpClientRequestContext requestContext, Request req, Task task) {
        String domain = task.getSite().getDomain();
        resetCookies(requestContext, domain);
    }

    @Override
    public void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
        // no-op
    }
    
    @Override
    public void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
        // no-op
    }
    
    private void resetCookies(HttpClientRequestContext requestContext, String domain) {
        HttpClientContext httpClientContext = requestContext.getHttpClientContext();
        CookieStore cookieStore = new BasicCookieStore();
        String ts = String.valueOf(System.currentTimeMillis() - 3000);
        cookieStore.addCookie(newCookie(domain, "freq", ts));
        cookieStore.addCookie(newCookie(domain, "fusion", ts));
        httpClientContext.setCookieStore(cookieStore);
    }

    private final static long ONE_YEAR = TimeUnit.DAYS.toMillis(365);
    
    private BasicClientCookie newCookie(String domain, String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, domain);
        cookie.setExpiryDate(new Date(System.currentTimeMillis() + ONE_YEAR));
        cookie.setPath("/");
        cookie.setVersion(0);
        cookie.setSecure(false);
        return cookie;
    }

}
