package com.jinguduo.spider.spider.mgtv;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.pipeline.ConsolePipeline;

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

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

@CommonsLog
public class MgtvEpiSpiderIT {
	
	@Autowired
	private MgtvEpiSpider mgtvEpiSpider;

	@Before
	public void setup()  {
	}

	@Test
	public void testContext() {
		Assert.notNull(mgtvEpiSpider);
	}

	/***
	 * @title 专辑页process
	 * @
     */
	@Test
	public void testRun()  {
		TestPipeline testPipeline = new TestPipeline();

		Job job = new Job("http://pcweb.api.mgtv.com/episode/list?collection_id=337307&page=1");
		job.setPlatformId(7);
		job.setShowId(1);
		DelayRequest delayRequest = new DelayRequest(job);

		SpiderEngine.create(mgtvEpiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs);
		jobs.forEach(j -> log.debug(j.toString()));

		List<Show> shows = resultItems.get(Show.class.getSimpleName());
		Assert.notEmpty(shows);
		shows.forEach(s -> log.debug(s.toString()));

	}
	@Test
	public void testZongYi() throws Exception {

		TestPipeline testPipeline = new TestPipeline();

		Job job = new Job("http://pcweb.api.mgtv.com/variety/showlist?collection_id=325963");
		job.setPlatformId(1);
		job.setShowId(1);
		job.setCode("325963");
		DelayRequest delayRequest = new DelayRequest(job);

		SpiderEngine.create(mgtvEpiSpider).addPipeline(testPipeline).addPipeline(new ConsolePipeline()).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.isTrue(
				CollectionUtils.isNotEmpty(jobs),
				"testZongYi process fail by url :" + job.getUrl()
		);
		List<Show> shows = resultItems.get(Show.class.getSimpleName());
		Assert.isTrue(
				CollectionUtils.isNotEmpty(shows),
				"testZongYi process fail by url :" + job.getUrl()
		);
	}
}
