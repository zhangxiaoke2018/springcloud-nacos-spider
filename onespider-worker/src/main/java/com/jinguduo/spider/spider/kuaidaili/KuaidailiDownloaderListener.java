package com.jinguduo.spider.spider.kuaidaili;

import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.cluster.downloader.listener.DownloaderListener;
import com.jinguduo.spider.common.type.ConcurrentHashMultiMap;
import com.jinguduo.spider.common.type.HashMultiMap;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/30
 * Time:18:45
 */
@CommonsLog
public class KuaidailiDownloaderListener implements DownloaderListener {
    private final static String DOMAIN = "kuaidaili.com";

    private final static long FIFTEEN_MINUTES = TimeUnit.MINUTES.toMillis(15);

    private Holder holder = new Holder();

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

    @Override
    public void onRequest(HttpClientRequestContext requestContext, Request req, Task task) {
        String url = req.getUrl();
        if (StringUtils.contains(url, "/#")) {
            String domain = task.getSite().getDomain();
            String cookie = null;
            try {
                cookie = URLDecoder.decode(StringUtils.substring(url, url.indexOf("/#") + 2), "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
            String newUrl = url.substring(0, url.indexOf("#"));
            req.setUrl(newUrl);
            CookieStore cookieStore = requestContext.getHttpClientContext().getCookieStore();
            if (cookieStore == null) {
                resetCookie(requestContext, domain, cookie);
            }
        }
    }

    private void resetCookie(HttpClientRequestContext requestContext, String domain, String cookie) {
        HttpClientContext httpClientContext = requestContext.getHttpClientContext();
        CookieStore cookieStore = holder.pop(domain);
        String yd_cookie = cookie.substring(cookie.indexOf("yd_cookie=") + 10, cookie.indexOf(";"));
        String clear = cookie.replace(yd_cookie, "");
        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
            setCookie(cookieStore, "bid", RandomStringUtils.randomAlphanumeric(11));
            setCookie(cookieStore, "ll", "0");
            setCookie(cookieStore, "yd_cookie", yd_cookie);
            setCookie(cookieStore, "_ydclearance", clear);
        }
        httpClientContext.setCookieStore(cookieStore);
    }

    private CookieStore setCookie(CookieStore cookieStore, String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(DOMAIN);
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, DOMAIN);
        cookie.setExpiryDate(new Date(System.currentTimeMillis() + FIFTEEN_MINUTES));
        cookie.setPath("/");
        cookie.setVersion(0);
        cookie.setSecure(false);
        cookieStore.addCookie(cookie);
        return cookieStore;
    }


    @Override
    public void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
//
    }

    @Override
    public void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
//
    }
}
