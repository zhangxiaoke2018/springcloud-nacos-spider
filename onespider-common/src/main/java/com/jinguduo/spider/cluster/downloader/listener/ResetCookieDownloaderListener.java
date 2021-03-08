package com.jinguduo.spider.cluster.downloader.listener;

import java.util.Random;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.common.type.Quota;
import com.jinguduo.spider.common.util.CookieMapper;
import com.jinguduo.spider.data.table.CookieString;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.slf4j.Slf4j;

/**
 * Cookie重置
 */
@Slf4j
public class ResetCookieDownloaderListener implements DownloaderListener {

    private Quota quota = null;
    // 需要检查的异常HttpStatuCode
    private Set<Integer> abnormalStatusCode = null;
    //
    private int bound = 100;
    private int prob = 0; // probability = 0%
    private Random random = new Random();
    
    @Override
    public void onRequest(HttpClientRequestContext requestContext, Request req, Task task) {
        // set Cookie
        if (prob == 100
                || (prob > 0 && random.nextInt(bound) < prob)) {
            resetCookieStore(requestContext);
        }
    }

    @Override
    public void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
        // log服务端Set-Cookie
        if (log.isDebugEnabled()) {
            String domain = task.getSite().getDomain();
            logSetCookie(requestContext, resp, domain);
        }
        
        final int statusCode = resp.getStatusLine().getStatusCode();
        if (abnormalStatusCode != null && abnormalStatusCode.contains(statusCode)) {
            resetCookieStore(requestContext);
        }
    }
    
    @Override
    public void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
        String domain = task.getSite().getDomain();
        // set cookies
        if ((quota != null && quota.isAboved(domain))) {
            resetCookieStore(requestContext);
        }
    }
    
    private void resetCookieStore(HttpClientRequestContext requestContext) {
        requestContext.getHttpClientContext().setCookieStore(new BasicCookieStore());
    }

    private void logSetCookie(HttpClientRequestContext requestContext, HttpResponse resp, String domain) {
        Header[] headers = resp.getHeaders("Set-Cookie");
        if (headers == null || headers.length == 0) {
            // no-op
            return;
        }
        CookieStore cookieStore = requestContext.getHttpClientContext().getCookieStore();
        CookieString cookieString = CookieMapper.writeCookieString(cookieStore, domain);
        log.info("Domain: " + domain + " Cookie: " + cookieString.getValue());
    }

    public ResetCookieDownloaderListener setQuota(Quota quota) {
        this.quota = quota;
        return this;
    }
    
    public ResetCookieDownloaderListener addAbnormalStatusCode(int... codes) {
        if (abnormalStatusCode == null) {
            abnormalStatusCode = Sets.newConcurrentHashSet();
        }
        for (int code : codes) {
            abnormalStatusCode.add(code);
        }
        return this;
    }
    
    /**
     * 
     * @param probability : between 0.00 and 1.00
     * @return
     */
    public ResetCookieDownloaderListener setProbability(double probability) {
        this.prob = (int)(probability * bound);  // probability ~ 0.xx
        return this;
    }
}
