package com.jinguduo.spider.spider.iqiyi;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

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
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class IqiyiApiSpiderIT {

	@Autowired
	private IqiyiApiSpider iqiyiApiSpider;


	@Before
	public void setup()  {
		
	}

	@Test
	public void testContext() {
		Assert.notNull(iqiyiApiSpider);
	}

	@Test
	public void testPc()  {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setUrl("http://cache.video.qiyi.com/jp/avlist/200455401/?albumId=200455401");
		job.setCode("200455401");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(iqiyiApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
//		Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()));

		Request request = resultItems.getRequest();
		Assert.isTrue("http://cache.video.qiyi.com/jp/pc/557734500/".equals(request.getUrl()));
	}
	
	@Test
	public void testSdvlst()  {
		TestPipeline testPipeline = new TestPipeline();
		
		Job job = new Job();
		job.setUrl("http://cache.video.qiyi.com/jp/sdvlst/6/233066601/");
		job.setCode("200455401");
		DelayRequest delayRequest = new DelayRequest(job);
		
		SpiderEngine.create(iqiyiApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		Assert.notNull(resultItems.get(Job.class.getSimpleName()));
	}

	@Test
	public void testAvlst()  {
		TestPipeline testPipeline = new TestPipeline();

		Job job = new Job();
		job.setUrl("http://cache.video.qiyi.com/jp/avlist/224717601/?albumId=224717601");
		job.setCode("224717601");
		DelayRequest delayRequest = new DelayRequest(job);

		SpiderEngine.create(iqiyiApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<Show> shows = resultItems.get(Show.class.getSimpleName());
		Assert.notNull(shows);
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notNull(jobs);

		shows.forEach(s -> log.debug(s.toString()));
		jobs.forEach(j -> log.debug(j.toString()));


	}

	@Test
	public void testLatest()  {
		Job job = new Job();
		job.setUrl("http://cache.video.qiyi.com/jp/sdvlst/latest?key=sdvlist&categoryId=27&sourceId=203966101&tvYear=201605");
		job.setCode("203966101");

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(iqiyiApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		Assert.notNull(resultItems);

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		List<Show> shows = resultItems.get(Show.class.getSimpleName());

		Assert.notEmpty(jobs);
		Assert.notEmpty(shows);
		System.out.println(jobs);
		System.out.println(shows);

	}

}
