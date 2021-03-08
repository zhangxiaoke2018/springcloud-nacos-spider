package com.jinguduo.spider.common.metric;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 假的自定义监控（用于测试和无自定义监控的环境）
 */
@CommonsLog
public class FakeMetricReportor implements MetricReportor {
    
    private Timer timer = new Timer();
    
    private long period = TimeUnit.MINUTES.toMillis(5);
    
    @Override
    public void start() {
        timer.schedule(new Sender(), period, period);
    }
    
    protected class Sender extends TimerTask {
        @Override
        public void run() {
            try {
            	Collection<MetricBean> metrics = MetricFactory.getAndResetMetrics();
            	if (log.isDebugEnabled()) {
            		log.debug("MetricBean size: " + metrics.size());
            		
            		for (MetricBean metric : metrics) {
            			log.debug(metric.toString());
            		}
            	}
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
