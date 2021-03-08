package com.jinguduo.spider;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.downloader.ImprovedDownloader;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.StorePipeline;
import com.jinguduo.spider.cluster.scheduler.DistributedScheduler;
import com.jinguduo.spider.cluster.worker.Heartbeat;
import com.jinguduo.spider.cluster.worker.SpiderWorker;
import com.jinguduo.spider.common.proxy.ProxyPoolManager;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

@Configuration
@ActiveProfiles("test")
public class TestConfig {

	@Mock
	private RestTemplate simpleHttp;
	
	@Value("${onespider.store.show_log.url}")
	private String showLogStoreUrl;
	
	@Value("${onespider.master.job.url}")
	private String jobStoreUrl;
	
	@Value("${onespider.master.jobs.sync.url}")
	private String jobSyncUrl;
	
	@Value("${onespider.master.worker.heartbeat.url}")
	private String heartbeatUrl;
	
	@Bean
	RestTemplate restTemplate() {
		return Mockito.mock(RestTemplate.class);
	}
	
	@Bean(name = "jobStorePipeline")
	public StorePipeline jobStorePipeline() {
		StorePipeline pipeline = new StorePipeline(simpleHttp, Job.class.getSimpleName(), jobStoreUrl);
		
		return pipeline;
	}
	
	@Bean(name = "showLogStorePipeline")
	public StorePipeline showLogStorePipeline() {
		StorePipeline pipeline = new StorePipeline(simpleHttp, ShowLog.class.getSimpleName(), showLogStoreUrl);
		
		return pipeline;
	}
	
	@Bean
	public DistributedScheduler distributedScheduler() {
		return new DistributedScheduler() {

			@Override
			public void push(Request request, Task task) {
				// nothing
			}

			@Override
			public Request poll(Task task) {
				// nothing
				return null;
			}

			@Override
			public void addSpiderWorker(SpiderWorker spiderWorker) {
				// nothing
			}

			@Override
			public void removeSpiderWorker(SpiderWorker spiderWorker) {
				// nothing
			}
		};
	}
	
	@Bean
	public Heartbeat heartbeat() {
		return new Heartbeat() {

			@Override
			public void start(SpiderWorker worker) {
				// nothing
			}
		};
	}
    
    @Bean
    public ImprovedDownloader downloaderManager() {
        DownloaderManager downloader = new DownloaderManager();
        return downloader;
    }
    
    @Bean
    public ProxyPoolManager proxyPoolManager() {
        return new ProxyPoolManager();
    }
}
