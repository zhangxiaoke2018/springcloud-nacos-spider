package com.jinguduo.spider.spider.weibo;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.type.Quota;
import com.jinguduo.spider.data.table.MediaAccount;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

public class WeiboCookieListener implements SpiderListener {

    private static Logger logger = LoggerFactory.getLogger(WeiboCookieListener.class);

    @Autowired(required=true)
    private OneLogin oneLogin;

    private MediaAccount mediaAccount = new MediaAccount();

    private Quota quota = null;

    // 需要检查的异常HttpStatuCode
    private Set<Integer> abnormalStatusCode = null;

    public WeiboCookieListener(Quota quota) {
        this.quota = quota;
    }

    @Override
    public void onRequest(Request request, Task task) {
        String domain = getDomain(task);
        if (!isValid()) {
            getMediaAccount(domain);
        }
        Site site = (Site)task.getSite();
        if (StringUtils.isNotBlank(mediaAccount.getCookie()))
            site
                    .addHeader("Cookie", mediaAccount.getCookie())
                    .setUserAgent(mediaAccount.getUserAgent());
        if (StringUtils.isNotBlank(mediaAccount.getHeaders())){
            try {
                Map<String,String> headers = (Map<String, String>) JSON.parse(mediaAccount.getHeaders());
                headers.entrySet().stream().forEach(s -> {
                    site
                            .addHeader(s.getKey(),s.getValue());
                });
            } catch (Exception e) {
                logger.warn("header illegality by username ;{} ",mediaAccount.getUserName());
            }
        }
    }

    @Override
    public void onResponse(Request request, Page page, Task task) {
        // check quota
        if (quota != null) {
            String domain = getDomain(task);
            final int statusCode = page.getStatusCode();
            if (abnormalStatusCode != null && abnormalStatusCode.contains(statusCode)) {
                if (quota.isAboved(domain)) {
                    getMediaAccount(domain);
                }
            } else {
                quota.reset(domain);
            }
        }
        this.oneLogin.onSuccess();
    }

    @Override
    public void onError(Request request, Exception e, Task task) {
        // check quota
        if (quota != null) {
            String domain = getDomain(task);
            if (quota.isAboved(domain)) {
                // reload and set cookies into table
                this.mediaAccount = null;
                getMediaAccount(domain);
            }
        }
        this.oneLogin.onInvalid();
    }

    public void onCookieInvalid(){
        this.mediaAccount = null;
        this.oneLogin.onInvalid();
    }

    private String getDomain(Task task) {
        if (task != null) {
            Site site = (Site)task.getSite();
            if (site != null) {
                String domain = site.getDomain();
                return domain == null ? "" : domain;
            }
        }
        return "";
    }

    public boolean isValid() {
        return ( null != mediaAccount && StringUtils.isNotBlank(mediaAccount.getCookie()))?true:false;
    }

    public MediaAccount getMediaAccount(String domain) {
        mediaAccount.setDomain(domain);
        return this.mediaAccount = oneLogin.build(this.mediaAccount);
    }

    @Override
    public void onStart(Task task) {
        // no-op
    }

    @Override
    public void onExit(Task task) {
        // no-op
    }
}
