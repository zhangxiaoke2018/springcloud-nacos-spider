package com.jinguduo.spider;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.cluster.downloader.ImprovedDownloader;
import com.jinguduo.spider.cluster.engine.SpiderEngineConfig;
import com.jinguduo.spider.cluster.scheduler.DistributedScheduler;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.cluster.spider.SpiderSettingLoader;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.cluster.worker.Heartbeat;
import com.jinguduo.spider.cluster.worker.SpiderDriver;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.proxy.ProxyPoolManager;
import com.jinguduo.spider.common.thread.AsyncTask;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;


@Component
public class WorkerRunner implements ApplicationRunner {

	@Autowired
	private List<Spider> spiders;
	
	@Autowired
	private SpiderSettingLoader spiderSettingLoader;

	@Autowired
	private Heartbeat heartbeat;

	@Autowired
	private DistributedScheduler scheduler;
	
	@Autowired
	private ImprovedDownloader downloader;

	@Autowired(required = false)
	private List<Pipeline> pipelines;
	
	@Autowired(required = false)
	private List<SpiderListener> spiderListeneres;
	
	@Autowired
	private ProxyPoolManager proxyPoolManager;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {

	    SpiderEngineConfig spiderEngineConfig = SpiderEngineConfig.builder()
	        .spiderSettingLoader(spiderSettingLoader)
	        .heartbeat(heartbeat)
	        .scheduler(scheduler)
	        .downloader(downloader)
	        .pipelines(pipelines)
	        .spiderListeneres(spiderListeneres)
	        .proxyPoolManager(proxyPoolManager)
	        .build();
	    
	    // 排序：为得到一个稳定可预期的启动顺序，便于优化
	    Iterator<Spider> iterator = spiders.stream().sorted(new Comparator<Spider>() {
			@Override
			public int compare(Spider a, Spider b) {
				return a.getSite().getDomain().compareTo(b.getSite().getDomain());
			}
	    }).iterator();
	    
	    AsyncTask async = new AsyncTask(4);
	    
	    final long wait = TimeUnit.SECONDS.toMillis(5);
	    
		while (iterator.hasNext()) {
			Spider spider = iterator.next();
			Worker worker = spider.getClass().getAnnotation(Worker.class);
			if (worker != null) {
				async.execute(new Runnable() {
                    @Override
                    public void run() {
                        SpiderDriver.start(spider, spiderEngineConfig);
                    }
			        
			    });
			    Thread.sleep(wait);  // 慢启动
			}
		}
		// clean up
		async.shutdown();
	}
}
