package com.jinguduo.spider.spider.tengxun;



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
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class TengxunPlayCountSpiderIT {
	
	@Autowired
	private TengxunPlayCountSpider tengxunPlayCountSpider;
	
	final static String ALBUM_ID = "k9gaf8f7u1ef8jz";
	final static String URL = String.format("http://sns.video.qq.com/tvideo/fcgi-bin/batchgetplaymount?id=%s&otype=json",
			ALBUM_ID);

	DelayRequest delayRequest;

	@Before
	public void setup()  {
		Job job = new Job();
		job.setPlatformId(1);
		job.setShowId(1);
		job.setUrl(URL);
		job.setFrequency(100);
		job.setMethod("GET");

		// request
		delayRequest = new DelayRequest(job);
	}

	@Test
	public void testContext() {
		Assert.notNull(tengxunPlayCountSpider);
	}


	@Test
	public void testRun() {
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(tengxunPlayCountSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()));

		Request request = resultItems.getRequest();
		Assert.isTrue(URL.equals(request.getUrl()));
	}
}
