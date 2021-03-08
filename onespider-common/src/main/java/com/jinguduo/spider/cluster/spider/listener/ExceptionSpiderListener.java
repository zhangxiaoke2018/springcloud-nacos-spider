package com.jinguduo.spider.cluster.spider.listener;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableSet;
import com.jinguduo.spider.common.type.TimedRateQuota;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 网络异常超过阈值，暂停爬虫请求
 * <p>在一定时间内(duration)网络异常的频率
 *    如果超过配置值(limit)则阻塞爬虫(blockMinutes)
 *    
 * <p>在一定时间内(duration)响应的HTTP Status Code次数
 *    如果超过配置值(limit)则阻塞爬虫一点时间(blockMinutes)
 *    
 * 
 *
 */
@Deprecated
@CommonsLog
public class ExceptionSpiderListener implements SpiderListener {
    
    private int limit = 15;
    private long duration = 1;
    private long blockMinutes = 7;
    
    private TimedRateQuota netExceptionQuota = new TimedRateQuota(duration, TimeUnit.MINUTES, limit);
    
    private TimedRateQuota statusCodeExceptionQuota = new TimedRateQuota(duration, TimeUnit.MINUTES, limit);
    
    private Map<String, Integer> memory = Collections.synchronizedMap(new HashMap<String, Integer>());
    
    private final Set<Integer> badStatusCodes = ImmutableSet.of(403, 405, 500, 502);
    
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public void onStart(Task task) {
        final String domain = task.getSite().getDomain();
        // 保存初始设置
        Integer sleepTime = task.getSite().getSleepTime();
        memory.put(domain, sleepTime);
    }

    @Override
    public void onRequest(Request req, Task task) {
        // nothing
    }
    
    @Override
    public void onResponse(Request req, Page page, Task task) {
        if (page == null || badStatusCodes.contains(page.getStatusCode())) {
            // 异常响应
            checkQuota(task, statusCodeExceptionQuota);
        }
    }
    
    @Override
    public void onError(Request req, Exception e, Task task) {
        if (e instanceof IOException) {
            // 只处理网络异常
            checkQuota(task, netExceptionQuota);
        }
    }
    
    @Override
    public void onExit(Task task) {
        // nothing
    }

    private void checkQuota(Task task, TimedRateQuota quota) {
        final String domain = task.getSite().getDomain();
        if (quota.isAboved(domain)) {
            // 增加sleep time以阻塞爬虫
            task.getSite().setSleepTime((int) TimeUnit.MINUTES.toMillis(blockMinutes));
            task.getSite().setRetrySleepTime((int) TimeUnit.MINUTES.toMillis(blockMinutes));
            log.warn(String.format("[Above of Quota] %s %s", domain, blockMinutes));
        } else {
            // 恢复初始sleep time设置
            Integer sleepTime = memory.get(domain);
            task.getSite().setSleepTime(sleepTime);
            task.getSite().setRetrySleepTime(sleepTime);
        }
    }
}
