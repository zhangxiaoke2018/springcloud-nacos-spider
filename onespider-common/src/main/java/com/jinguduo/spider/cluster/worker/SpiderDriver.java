package com.jinguduo.spider.cluster.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;

import com.jinguduo.spider.cluster.engine.ElasticThreadStrategy;
import com.jinguduo.spider.cluster.engine.FixedThreadStrategy;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.engine.SpiderEngineConfig;
import com.jinguduo.spider.cluster.engine.ThreadStrategy;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.common.metric.MetricFactory;
import com.jinguduo.spider.common.metric.Metrizable;
import com.jinguduo.spider.common.util.HostUtils;
import com.jinguduo.spider.data.table.SpiderSetting;

import lombok.extern.slf4j.Slf4j;

/**
 * 启动或停止SpiderEngine
 */
@Slf4j
public class SpiderDriver {
    
    // domain -> SpiderWorker
    private final static Map<String, SpiderWorker> pool = new ConcurrentHashMap<>();
    
	public static SpiderWorker start(Spider spider, SpiderEngineConfig config) {
	    // 
	    SpiderWorker spiderWorker = null;
	    String domain = spider.getSite().getDomain();
	    synchronized (domain) {
	        spiderWorker = pool.get(domain);
	        if (spiderWorker != null) {
	            pool.remove(domain);
	            terminate(spiderWorker);
            }
	        spiderWorker = init(spider, config);
	        run(spiderWorker);
	        pool.put(domain, spiderWorker);
        }
	    return spiderWorker;
	}
	
	public static void run(SpiderWorker spiderWorker) {
	    spiderWorker.getSpiderEngine().start();
	    spiderWorker.getHeartbeat().start(spiderWorker);
	    spiderWorker.getScheduler().addSpiderWorker(spiderWorker);
	    
	    spiderWorker.setCommand(WorkerCommand.Run);
	}
	
	private static synchronized SpiderWorker init(Spider spider, SpiderEngineConfig config) {
	    String uuid = RandomStringUtils.randomAlphanumeric(5);
	    
	    Site site = spider.getSite();
        String domain = site.getDomain();
	    // initialization
	    SpiderSetting spiderSetting = config.getSpiderSettingLoader().load(spider);
	    
	    Integer sleepTime = spiderSetting.getSleepTime();
	    int cycleRetryTimes = spiderSetting.getCycleRetryTimes();
	    if (spiderSetting.getHttpProxyEnabled()) {
	        site.setProxyPool(config.getProxyPoolManager().getHttpProxyPool());
	        cycleRetryTimes *= 2;  // 匿名代理增加重试次数
	    } else if (spiderSetting.getVpsHttpProxyEnabled()) {
			site.setProxyPool(config.getProxyPoolManager().getVpsProxyPool());
		} else if (spiderSetting.getKdlHttpProxyEnabled()) {
			site.setProxyPool(config.getProxyPoolManager().getKuaidailiProxyPool());
		}
	    
	    site.setFrequency(spiderSetting.getFrequency());
	    site.setSleepTime(sleepTime);
	    site.setTimeOut(spiderSetting.getTimeOut());
	    site.setRetryTimes(spiderSetting.getRetryTimes());
	    site.setCycleRetryTimes(cycleRetryTimes);
	    site.setRetryDelayTime(spiderSetting.getRetryDelayTime());
	    
	    // add aliyun metric
	    Metrizable pageCounter = MetricFactory.builder()
	    		.namespace("onespider_engine")
                .metricName("page_count")
	            .addDimension("Domain", domain)
	            .addDimension("Host", HostUtils.getHostName())
	            .build();
	    
	    // thread strategy
	    String threadName = "SpiderEngine-" + domain.replace('.', '-');
	    String threadPoolName = threadName + "-t";
	    Integer threadNum = spiderSetting.getThreadNum();
	    ThreadStrategy threadStrategy = null;
	    if (threadNum > 0) {
	    	threadStrategy = new FixedThreadStrategy(threadNum, threadPoolName);
		} else {
			threadStrategy = new ElasticThreadStrategy(threadPoolName);
		}
	    
	    SpiderEngine spiderEngine = SpiderEngine
	            .create(spider)
	            .setUUID(uuid)
	            .setPipelines(config.getPipelines())
	            .setScheduler(config.getScheduler())
	            .setDownloader(config.getDownloader())
	            .setSpiderListeners(config.getSpiderListeneres())
	            .addSpiderListeners(site.getSpiderListeners())
	            .thread(threadStrategy, threadNum)
	            .setEmptySleepTime(spiderSetting.getEmptySleepTime())
	            .setExitWhenComplete(false)
	            .setSpawnUrl(false)
	            .setPageCounter(pageCounter)
	            .setThreadName(threadName);
	    
	    SpiderWorker spiderWorker = SpiderWorker.builder()
	        .spider(spider)
	        .spiderEngineConfig(config)
	        .spiderEngine(spiderEngine)
	        .heartbeat(config.getHeartbeat())
	        .scheduler(config.getScheduler())
	        .uuid(uuid)
	        .command(WorkerCommand.Noop)
	        .syncVersion(0)
	        .build();
	    
	    pool.put(domain, spiderWorker);
	    
	    log.info("Spider init " + site.toString());
	    log.info("Spider init " + spiderSetting.toString());
	    
        return spiderWorker;
    }

    public static void perform(SpiderWorker spiderWorker, WorkerCommand command) throws Exception {
		switch (command) {
		case Run:
			run(spiderWorker);
			break;
		case Terminate:
			terminate(spiderWorker);
			break;
		case Restart:
            terminate(spiderWorker);
            start(spiderWorker.getSpider(), spiderWorker.getSpiderEngineConfig());
            break;
		default:
			break;
		}
	}

	public static void terminate(SpiderWorker spiderWorker) {
		spiderWorker.getScheduler().removeSpiderWorker(spiderWorker);
		spiderWorker.getSpiderEngine().close();
		spiderWorker.getSpiderEngine().stop();
		spiderWorker.setCommand(WorkerCommand.Terminate);
	}
}
