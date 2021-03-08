package com.jinguduo.spider.spider.mgtv;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class MgtvPlayCount2SpiderIT {
	
	@Autowired
	private MgtvPlayCount2Spider mgtvSpider;

	final static String URL = "https://vc.mgtv.com/v2/dynamicinfo?cid=336825&vid=";

	DelayRequest delayRequest;

	@Before
	public void setup()  {
		Job job = new Job(URL);
		job.setPlatformId(1);
		job.setShowId(1);
		job.setFrequency(100);

		delayRequest = new DelayRequest(job);
	}

	@Test
	public void testContext() {
		Assert.notNull(mgtvSpider);
	}

	@Test
	public void testRun() {
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(mgtvSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()));

	}

	@Test
	public void testMovie() {
		Job job = new Job("https://vc.mgtv.com/v2/dynamicinfo?vid=3760242");
		job.setPlatformId(1);
		job.setShowId(1);
		job.setFrequency(100);
		job.setCode("309928");

		DelayRequest dr = new DelayRequest(job);

		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(mgtvSpider).addPipeline(testPipeline).addRequest(dr).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
		Assert.notNull(showLogs);
		System.out.println(showLogs);

	}
}
