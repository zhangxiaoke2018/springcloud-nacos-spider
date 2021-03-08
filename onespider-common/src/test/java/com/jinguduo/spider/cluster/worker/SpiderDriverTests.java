package com.jinguduo.spider.cluster.worker;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.cluster.downloader.ImprovedDownloader;
import com.jinguduo.spider.cluster.engine.SpiderEngineConfig;
import com.jinguduo.spider.cluster.scheduler.DistributedScheduler;
import com.jinguduo.spider.cluster.spider.DefaultSpiderSettingLoader;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.cluster.worker.Heartbeat;
import com.jinguduo.spider.cluster.worker.SpiderDriver;
import com.jinguduo.spider.cluster.worker.SpiderWorker;
import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.proxy.ProxyPoolManager;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SpiderDriverTests {

	@Mock
	private RestTemplate restTemplate;
	
	@Autowired
	protected Heartbeat heartbeat;

	@Autowired
	protected DistributedScheduler scheduler;

	@Autowired
	List<Pipeline> pipelines;
	
	@Autowired(required = false)
	protected List<SpiderListener> spiderListeneres;
	
	@Autowired
    private ImprovedDownloader downloader;
	
	@Autowired
	private ProxyPoolManager proxyPoolManager;
	
	private SpiderEngineConfig spiderEngineConfig;
	
	@Before
	public void setup() throws Exception {
	    spiderEngineConfig = SpiderEngineConfig.builder()
	            .spiderSettingLoader(new DefaultSpiderSettingLoader())
	            .heartbeat(heartbeat)
	            .scheduler(scheduler)
	            .downloader(downloader)
	            .pipelines(pipelines)
	            .spiderListeneres(spiderListeneres)
	            .proxyPoolManager(proxyPoolManager)
	            .build();
	}
	
	@Test
	public void contextLoads() {
	}
	
	@Test
	public void testRun() throws Exception {
		SpiderWorker spiderWorker = SpiderDriver.start(new TestSpider(), spiderEngineConfig);
		Thread.sleep(5000);
		Assert.isTrue(spiderWorker.getStatus() == SpiderStatus.Running);
		Assert.isTrue(spiderWorker.getSpiderEngine().getStatus() == SpiderStatus.Running);
	}
	
	@Test
    public void testStart2() throws Exception {
	    TestSpider spider = new TestSpider();
        SpiderWorker spiderWorker = SpiderDriver.start(spider, spiderEngineConfig);
        Thread.sleep(5000); // wait
        Assert.isTrue(spiderWorker.getStatus() == SpiderStatus.Running);
        Assert.isTrue(spiderWorker.getSpiderEngine().getStatus() == SpiderStatus.Running);
        
        spiderWorker = SpiderDriver.start(spider, spiderEngineConfig);
        Thread.sleep(1000); // wait
        Assert.isTrue(spiderWorker.getStatus() == SpiderStatus.Running);
        Assert.isTrue(spiderWorker.getSpiderEngine().getStatus() == SpiderStatus.Running);
    }
	
	@Test
	public void testRestart() throws Exception {
	    SpiderWorker spiderWorker = SpiderDriver.start(new TestSpider(), spiderEngineConfig);
		Thread.sleep(5000); // wait
		Assert.isTrue(spiderWorker.getStatus() == SpiderStatus.Running);
		Assert.isTrue(spiderWorker.getSpiderEngine().getStatus() == SpiderStatus.Running);
		
		SpiderDriver.terminate(spiderWorker);
		Thread.sleep(1000); // wait
		Assert.isTrue(spiderWorker.getStatus() == SpiderStatus.Stopped);
		Assert.isTrue(spiderWorker.getSpiderEngine().getStatus() == SpiderStatus.Stopped);
		
		SpiderDriver.run(spiderWorker);
		Thread.sleep(1000); // wait
		Assert.isTrue(spiderWorker.getStatus() == SpiderStatus.Running);
		Assert.isTrue(spiderWorker.getSpiderEngine().getStatus() == SpiderStatus.Running);
	}
	
	@Test
	public void testTerminate() throws Exception {
	    SpiderWorker spiderWorker = SpiderDriver.start(new TestSpider(), spiderEngineConfig);
		Thread.sleep(1000); // wait
		
		Assert.isTrue(spiderWorker.getStatus() == SpiderStatus.Running);
		Assert.isTrue(spiderWorker.getSpiderEngine().getStatus() == SpiderStatus.Running);
		
		Thread.sleep(1000); // wait
		SpiderDriver.terminate(spiderWorker);
	}
	
	class TestSpider implements Spider {
		private Site site = SiteBuilder.builder().setDomain(UUID.randomUUID().toString()).build();
		
		@Override
		public void process(Page page) {
			// nothing
		}
		
		@Override
		public Site getSite() {
			return site;
		}
	}
}
