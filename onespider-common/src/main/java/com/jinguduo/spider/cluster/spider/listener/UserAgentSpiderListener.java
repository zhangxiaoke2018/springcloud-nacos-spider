package com.jinguduo.spider.cluster.spider.listener;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.common.constant.UserAgentKind;
import com.jinguduo.spider.common.type.Quota;
import com.jinguduo.spider.data.loader.UserAgentStoreLoader;
import com.jinguduo.spider.data.table.UserAgent;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 动态更换爬虫User-Agent
 *
 */
@CommonsLog
public class UserAgentSpiderListener implements SpiderListener {

	@Autowired(required = false)
    private UserAgentStoreLoader userAgentStoreLoader;

    private Quota quota = null;
    // 需要检查的异常HttpStatuCode
    private Set<Integer> abnormalStatusCode = null;
    
    private volatile boolean mustBeLoaded = true;
    private volatile long lastestReloadTime = 0;
    private long reloadOffsetTimeMills = TimeUnit.MINUTES.toMillis(60); // mills
    
    private RandomBucket userAgentBucket = new RandomBucket();
    
    private UserAgentKind kind = UserAgentKind.PC;
    
    public UserAgentSpiderListener() {
    }
    
    public UserAgentSpiderListener(Quota quota) {
        this.quota = quota;
    }
    
    public UserAgentSpiderListener(UserAgentKind kind) {
        this.kind = kind;
    }
    
    public UserAgentSpiderListener(Quota quota, UserAgentKind kind) {
        this.quota = quota;
        this.kind = kind;
    }

    @Override
    public void onStart(Task task) {
        reloadUserAgents(kind);
    }

    @Override
    public void onRequest(Request req, Task task) {
        // load
        if (needToReload()) {
            reloadUserAgents(kind);
        }
        // add change
        String ua = userAgentBucket.getOne();
        if (StringUtils.hasText(ua)) {
            task.getSite().setUserAgent(ua);
        }
    }

    private boolean needToReload() {
        if (!mustBeLoaded || isTimeToReload()) {
            synchronized (this) {
                if (!mustBeLoaded) {
                    mustBeLoaded = true;
                    lastestReloadTime = System.currentTimeMillis();
                }
            }
        }
        return mustBeLoaded;
    }

    private boolean isTimeToReload() {
        return (lastestReloadTime + reloadOffsetTimeMills) < System.currentTimeMillis();
    }

    @Override
    public void onResponse(Request req, Page page, Task task) {
        // check quota
        if (quota != null) {
            String domain = getDomain(task);
            final int statusCode = page.getStatusCode();
            if (abnormalStatusCode != null && abnormalStatusCode.contains(statusCode)) {
                if (quota.isAboved(domain)) {
                    // reload and set into cookies table
                    reloadUserAgents(kind);
                }
            } else {
                quota.reset(domain);
            }
        }
    }

    @Override
    public void onError(Request req, Exception e, Task task) {
        // check quota
        if (quota != null) {
            String domain = getDomain(task);
            if (quota.isAboved(domain)) {
                // reload and set cookies into table
                reloadUserAgents(kind);
            }
        }
    }
    
    @Override
    public void onExit(Task task) {
        // no-op
    }

    private void reloadUserAgents(UserAgentKind kind) {
        log.debug("Reload User Agents");
        if (userAgentStoreLoader == null) {
            log.warn("The UserAgentStoreLoader is null.");
            return;
        }
        Collection<UserAgent> newUserAgents = userAgentStoreLoader.load(kind);
        userAgentBucket.putAll(newUserAgents);
        mustBeLoaded = false;
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

    public UserAgentSpiderListener setUserAgentsStoreLoader(UserAgentStoreLoader userAgentStoreLoader) {
        this.userAgentStoreLoader = userAgentStoreLoader;
        return this;
    }

    public UserAgentSpiderListener setQuota(Quota quota) {
        this.quota = quota;
        return this;
    }
    
    public UserAgentSpiderListener addAbnormalStatusCode(int... codes) {
        if (abnormalStatusCode == null) {
            abnormalStatusCode = Sets.newConcurrentHashSet();
        }
        for (int code : codes) {
            abnormalStatusCode.add(code);
        }
        return this;
    }
    
    class RandomBucket {
        private volatile String[] bucket = null;
        private final Random rnd = new Random();

        public void putAll(Collection<UserAgent> newUserAgents) {
            if (newUserAgents != null && !newUserAgents.isEmpty()) {
                bucket = newUserAgents.stream().map(v -> v.getValue()).toArray(String[]::new);
            }
        }

        public String getOne() {
            final String[] u = bucket;
            if (u == null || u.length == 0) {
                return null;
            }
            return u[rnd.nextInt(u.length)];
        }
    }

}
