package com.jinguduo.spider.spider.pptv;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

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
public class PptvBannerSpiderIT {


	final static String BANNER_TEST_URL = "http://v.pptv.com/show/vicvUUrogkM4xrxc.html";
	
	@Autowired
	private PptvBannerSpider pptvBannerSpider;
	
	@Test
	public void testContext() {
		Assert.notNull(pptvBannerSpider);
	}

	@Test
	public void testBannerDrama()  {
	    Job testJob = new Job("http://tv.pptv.com/");
	    testJob.setPlatformId(7);
	    testJob.setShowId(1);
	    testJob.setFrequency(100);
	    testJob.setCode("1652093585");

        DelayRequest delayRequest = new DelayRequest(testJob);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(pptvBannerSpider).addPipeline(testPipeline).addPipeline(new ConsolePipeline()).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        
        Assert.notEmpty(jobs);
        Assert.notEmpty(shows);
        shows.forEach(s->log.debug(s.toString()));
	}
}
