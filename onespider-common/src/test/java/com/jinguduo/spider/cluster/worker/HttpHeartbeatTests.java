package com.jinguduo.spider.cluster.worker;

import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.cluster.worker.HttpHeartbeat;
import com.jinguduo.spider.cluster.worker.SpiderWorker;
import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.constant.WorkerCommand;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class HttpHeartbeatTests {
	
	@Value("${onespider.master.worker.heartbeat.url}")
	protected String heartbeatUrl;

	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private SpiderWorker worker;
	
	@Mock
	private Spider spider;
	
	@Mock
	private Site site;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(worker.getUuid()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(worker.getStatus()).thenReturn(SpiderStatus.Running);
		Mockito.when(worker.getCommand()).thenReturn(WorkerCommand.Noop);
		Mockito.when(worker.getSpider()).thenReturn(spider);
		
		Mockito.when(spider.getSite()).thenReturn(site);
		Mockito.when(site.getDomain()).thenReturn("www.HttpHeartbeatTests.com");
	}
	
	@Test
	public void contextLoads() {
	}
	
	@Ignore("long time")
	@Test
	public void testStart() throws InterruptedException {
		Mockito.when(restTemplate.getForObject("test",Mockito.anyObject(), Mockito.eq(WorkerCommand.class))).thenReturn(WorkerCommand.Noop);
		
		HttpHeartbeat heartbeat = new HttpHeartbeat(restTemplate, heartbeatUrl);
		heartbeat.setHeartbeatDelay(0);
		heartbeat.setHeartbeatPeriod(15000);
		heartbeat.start(worker);
		
		//Thread.sleep(2000000);
		// TODO: continue
	}
}
