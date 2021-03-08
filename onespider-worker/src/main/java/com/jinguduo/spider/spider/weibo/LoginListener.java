package com.jinguduo.spider.spider.weibo;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.type.Quota;
import com.jinguduo.spider.data.table.MediaAccount;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.apachecommons.CommonsLog;

@Deprecated
@CommonsLog
public class LoginListener implements SpiderListener {

    @Autowired
    private WeiboLogin weiboLogin;

    private MediaAccount mediaAccount;

    private Quota quota = null;

    private Set<Integer> abnormalStatusCode = null;

    private volatile boolean mustBeLoaded = true;

    public LoginListener(Quota quota) {
        this.quota = quota;
    }

    @Override
    public void onRequest(Request req, Task task) {
        if (!isCookieValid()) {
            login();
        }
        log.debug("login cookie : " + mediaAccount.getCookie());
        task.getSite().addHeader("Cookie", mediaAccount.getCookie());
        //task.getSite().addCookie(domain, cookie.getKey(), cookie.getValue());
    }

    @Override
    public void onResponse(Request request, Page page, Task task) {

        if (quota != null) {
            String domain = getDomain(task);
            final int statusCode = page.getStatusCode();
            if (abnormalStatusCode != null && abnormalStatusCode.contains(statusCode)) {
                if (quota.isAboved(domain)) {
                    login();
                }
            } else {
                quota.reset(domain);
            }
        }
    }

    @Override
    public void onError(Request req, Exception e, Task task) {
        if (quota != null) {
            String domain = getDomain(task);
            if (quota.isAboved(domain)) {
                weiboLogin.onCookieInvalid();
            }
        }
    }

    private void login() {
        mediaAccount = weiboLogin
                .setAccountType(1)
                .setWorkIp(null)
                .buildMediaAccount();
    }

    public void onCookieInvalid() {
        weiboLogin.onCookieInvalid();
    }

    private static String getDomain(Task task) {
        if (task != null) {
            Site site = (Site)task.getSite();
            if (site != null) {
                String domain = site.getDomain();
                return domain == null ? "" : domain;
            }
        }
        return "";
    }

    public boolean isCookieValid() {
        return StringUtils.isNotBlank(Weibo.cookies_str);
    }

    public LoginListener setQuota(Quota quota) {
        this.quota = quota;
        return this;
    }

    public LoginListener addAbnormalStatusCode(int... codes) {
        if (abnormalStatusCode == null) {
            abnormalStatusCode = Sets.newConcurrentHashSet();
        }
        for (int code : codes) {
            abnormalStatusCode.add(code);
        }
        return this;
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
