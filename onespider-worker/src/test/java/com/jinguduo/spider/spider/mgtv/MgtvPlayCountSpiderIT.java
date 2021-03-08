package com.jinguduo.spider.spider.mgtv;



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class MgtvPlayCountSpiderIT {
	
	@Autowired
	private MgtvPlayCountSpider mgtvSpider;

	final static String URL = "http://videocenter-2039197532.cn-north-1.elb.amazonaws.com.cn//dynamicinfo?callback=jQuery18209559400354382939_1466759714593&vid=4711756";

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
		Job job = new Job("http://videocenter-2039197532.cn-north-1.elb.amazonaws.com.cn/dynamicinfo?callback=jQuery18209201952717231603_1468220934000&cid=325963&_=1468220935548&vid=4590972");
		job.setPlatformId(1);
		job.setShowId(1);
		job.setFrequency(100);
		job.setCode("318423");

		DelayRequest dr = new DelayRequest(job);

		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(mgtvSpider).addPipeline(testPipeline).addRequest(dr).run();

		ResultItems resultItems = testPipeline.getResultItems();
		Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()));

	}
}
