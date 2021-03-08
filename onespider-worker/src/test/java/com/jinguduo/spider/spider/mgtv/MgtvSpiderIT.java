package com.jinguduo.spider.spider.mgtv;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.pipeline.ConsolePipeline;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

@CommonsLog
public class MgtvSpiderIT {
	
	@Autowired
	private MgtvSpider mgtvSpider;

	@Before
	public void setup()  {
	}

	@Test
	public void testContext() {
		Assert.notNull(mgtvSpider);
	}

	@Test
    public void testBannerDrama()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job("https://www.mgtv.com/h/337307.html");
        job.setPlatformId(7);
        job.setShowId(1);
        job.setCode("337307");
        DelayRequest delayRequest = new DelayRequest(job);

        SpiderEngine.create(mgtvSpider)
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        Assert.notEmpty(jobs);
        Assert.notEmpty(shows);
        shows.forEach(s->log.debug(s.toString()));
    }


	/***
	 * @title 专辑页process
	 * @
     */
	@Test
	public void testItemProcess()  {
		TestPipeline testPipeline = new TestPipeline();

		Job job = new Job("https://www.mgtv.com/h/333900.html");
		job.setPlatformId(1);
		job.setShowId(1);
		job.setCode("333900");
		DelayRequest delayRequest = new DelayRequest(job);

		SpiderEngine.create(mgtvSpider)
				.addPipeline(testPipeline)
				.addPipeline(new ConsolePipeline())
				.addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.isTrue(
				CollectionUtils.isNotEmpty(jobs),
				"item process fail by url :" + job.getUrl()
		);
	}

	/**
	 * 视频详情页
	 * @
     */
	@Test
	public void testKey()  {
		TestPipeline testPipeline = new TestPipeline();
		
		Job job = new Job("http://vc.mgtv.com/v2/dynamicinfo?cid=293919");
		job.setPlatformId(7);
		job.setShowId(1);
		job.setCode("293919");
		DelayRequest delayRequest = new DelayRequest(job);
		
		SpiderEngine.create(mgtvSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		//Assert.notNull(resultItems.get(Job.class.getSimpleName()));

		//Request request = resultItems.getRequest();
		//Assert.isTrue(URL.equals(request.getUrl()));
	}

	@Test
	public void testZongyi() throws Exception {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job("https://www.mgtv.com/b/328378/7738315.html");
		job.setPlatformId(7);
		job.setShowId(1);
		job.setCode("7738315");
		DelayRequest delayRequest = new DelayRequest(job);

		SpiderEngine.create(mgtvSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		Assert.notNull(resultItems);

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.isTrue(jobs.size() == 2);

	}

	@Test
	public void testMovie() throws Exception {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job("https://www.mgtv.com/b/325269/4563269.html?cxid=90f0zbamf");
		job.setPlatformId(7);
		job.setShowId(1);
		job.setCode("316982");
		DelayRequest delayRequest = new DelayRequest(job);

		SpiderEngine.create(mgtvSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		Assert.notNull(resultItems);

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.isTrue(jobs.size() == 2);

	}



}
