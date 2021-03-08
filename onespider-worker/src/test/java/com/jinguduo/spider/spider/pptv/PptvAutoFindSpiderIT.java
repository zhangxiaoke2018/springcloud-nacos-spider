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
public class PptvAutoFindSpiderIT {
	
	@Autowired
	private PptvAutoFindSpider pptvAutoFindSpider;
	
	private static final String MOVIE_URL = "http://list.pptv.com/?type=1&sort=time&area=5&year=2017&contype=0";
	private static final String DRAMA_URL = "http://list.pptv.com/?type=2&year=2017&sort=time&status=4&area=5";

	@Test
	public void testContext() {
		Assert.notNull(pptvAutoFindSpider);
	}

	@Test
	public void testAutoFindProcess()  {
	    Job testJob = new Job(DRAMA_URL);
	    testJob.setPlatformId(7);
	    testJob.setShowId(1);
	    testJob.setFrequency(100);
	    testJob.setCode("1652093585");

        DelayRequest delayRequest = new DelayRequest(testJob);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(pptvAutoFindSpider).addPipeline(testPipeline).addPipeline(new ConsolePipeline()).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        
        Assert.notEmpty(jobs);
        Assert.notEmpty(shows);
        log.debug(shows.size());
	}
}
