package com.jinguduo.spider.cluster.pipeline;

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

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.StorePipeline;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.Task;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class StorePipelineTests {
	
	@Mock
	private RestTemplate simpleHttp;

	@Value("${onespider.master.job.url}")
	private String storeJobUrl;

	private ResultItems resultItems = new ResultItems();

	@Mock
	private Task task;

	@Before
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);

		ArrayList<Job> jobs = new ArrayList<Job>();
		resultItems.put(Job.class.getSimpleName(), jobs);

		Job job = new Job("http://www.AsyncStorePipelineTests.com/1");
		jobs.add(job);

		job = new Job("http://www.AsyncStorePipelineTests.com/2");
		jobs.add(job);

		Mockito.when(task.toString()).thenReturn("");
	}

	@Test
	public void testProcess() throws InterruptedException {
		Mockito.when(simpleHttp.postForObject(Mockito.eq(storeJobUrl), Mockito.anyObject(), Mockito.eq(String.class)))
				.thenAnswer(new Answer<String>() {
					@Override
					public String answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						Assert.isTrue(args[1] instanceof Job);

						return "";
					}
				});

		StorePipeline pipeline = new StorePipeline(simpleHttp, Job.class.getSimpleName(), storeJobUrl);

		pipeline.process(resultItems, task);
		Thread.sleep(1200L);
	}
}
