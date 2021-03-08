package com.jinguduo.spider.spider.mgtv;


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
public class MgtvAutoFindSpiderIT {
	
	@Autowired
	private MgtvAutoFindSpider mgtvAutoFindSpider;
	
	private static final String MOVIE_URL = "http://list.mgtv.com/3/a4-49-------2848093-2-1--a1-.html?channelId=3";
	private static final String DRAMA_URL = "http://list.mgtv.com/2/a4-49--------2-1--a1-.html?channelId=2";
	private static final String VARIETY_URL = "http://list.mgtv.com/1/a4-49--------2-1--a1-.html?channelId=1";
	private static final String KID_ANIME_URL ="https://list.mgtv.com/10/a1---a1------c1-1---.html?channelId=10";

	@Test
	public void testContext() {
		Assert.notNull(mgtvAutoFindSpider);
	}

	@Test
	public void testAutoFindProcess()  {
	    Job testJob = new Job(KID_ANIME_URL);
	    testJob.setPlatformId(7);
	    testJob.setShowId(1);
	    testJob.setFrequency(100);
	    testJob.setCode("1652093585");

        DelayRequest delayRequest = new DelayRequest(testJob);

        TestPipeline testPipeline = new TestPipeline();

        SpiderEngine.create(mgtvAutoFindSpider)
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(delayRequest)
                .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        
        Assert.notEmpty(jobs);
        Assert.notEmpty(shows);
        log.debug(shows.size());
	}
}
