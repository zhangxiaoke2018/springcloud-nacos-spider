package com.jinguduo.spider.cluster.scheduler;

import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobPackage;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.scheduler.DelayRequestScheduler;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.cluster.worker.SpiderWorker;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.constant.JobSchedulerCommand;
import com.jinguduo.spider.webmagic.Task;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DelayRequestSchedulerTests {

	@Mock
	private RestTemplate restTemplate;
	
	@Value("${onespider.master.jobs.sync.url}")
	private String jobUrl;
	
	@Mock
	private SpiderWorker worker;
	
	@Mock
	private Spider spider;
	
	@Mock
	private SpiderEngine spiderEngine;
	
	@Mock
	private Task task;
	
	@Mock
	private Site site;
	
	private JobPackage jobSyncPackage0;
	private String uuid = "test";
	private int syncVersion = 0;
	private String domain = "www.DelayRequestSchedulerTests.com";
	
	@Before
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
		
		Mockito.doNothing().when(worker).setSyncVersion(Mockito.anyInt());
		Mockito.when(worker.getUuid()).thenReturn(uuid);
		Mockito.when(worker.getSpider()).thenReturn(spider);
		Mockito.when(worker.getSpiderEngine()).thenReturn(spiderEngine);
		Mockito.when(worker.getSyncVersion()).thenAnswer(new Answer<Integer>() {
			private int counter = syncVersion;
			public Integer answer(InvocationOnMock invocation) {
				return counter == 0 ? counter++ : 1;
			}
		});
		
		Mockito.when(spider.getSite()).thenReturn(site);
		Mockito.when(task.getSite()).thenReturn(site);
		Mockito.when(site.getDomain()).thenReturn(domain);
		
		jobSyncPackage0 = new JobPackage();
		jobSyncPackage0.setDomain(domain);
		jobSyncPackage0.setVersion(syncVersion + 1);
		jobSyncPackage0.setWorkerUuid(uuid);
		jobSyncPackage0.setJobs(new ArrayList<Job>());
		
		Job job = new Job("http://" + domain + "/1", "GET");
		job.setId(String.valueOf(1));
		job.setFrequency(1);
		job.setKind(JobKind.Forever);
		job.setCommand(JobSchedulerCommand.Add);
		jobSyncPackage0.getJobs().add(job);
		
		job = new Job("http://"+ domain +"/2", "GET");
		job.setId(String.valueOf(2));
		job.setFrequency(2);
		job.setKind(JobKind.Forever);
		job.setCommand(JobSchedulerCommand.Update);
		jobSyncPackage0.getJobs().add(job);
	}
	
	@Test
	public void testRun() throws InterruptedException, NoSuchFieldException, SecurityException {
		String uri0 = UriComponentsBuilder.fromHttpUrl(jobUrl)
				.queryParam("uuid", uuid)
				.queryParam("version", syncVersion).build().encode().toString();
		Mockito.when(restTemplate.postForObject(Mockito.eq(uri0), Mockito.any(), Mockito.eq(JobPackage.class))).thenReturn(jobSyncPackage0);
		
		String uri1 = UriComponentsBuilder.fromHttpUrl(jobUrl)
				.queryParam("uuid", uuid)
				.queryParam("version", 1).build().encode().toString();
		Mockito.when(restTemplate.postForObject(Mockito.eq(uri1), Mockito.any(), Mockito.eq(JobPackage.class))).thenReturn(null);
		
		
		DelayRequestScheduler scheduler = new DelayRequestScheduler(100, 200);
		scheduler.addSpiderWorker(worker);
		scheduler.setRestTemplate(restTemplate);
		scheduler.setJobUrl(jobUrl);
		
		DelayRequest req = (DelayRequest)scheduler.poll(task);
		Assert.isNull(req);
		
		Thread.sleep(500);
		
		req = (DelayRequest)scheduler.poll(task);
        Assert.notNull(req);
        req = (DelayRequest)scheduler.poll(task);
        Assert.notNull(req);
        
        Thread.sleep(1200);
		
		req = (DelayRequest)scheduler.poll(task);
		Assert.notNull(req);
		Assert.notNull(req.getUrl());
		Assert.notNull(req.getJob());
		Assert.isTrue("1".equals(req.getJob().getId()));
		
		req = (DelayRequest)scheduler.poll(task);
		Assert.isNull(req);
		
		Thread.sleep(1100);
		
		req = (DelayRequest)scheduler.poll(task);
		Assert.notNull(req);
		Assert.notNull(req.getUrl());
		Assert.notNull(req.getJob());
		
		req = (DelayRequest)scheduler.poll(task);
		Assert.notNull(req);
		Assert.notNull(req.getUrl());
		Assert.notNull(req.getJob());
		
		req = (DelayRequest)scheduler.poll(task);
		Assert.isNull(req);
	}
}
