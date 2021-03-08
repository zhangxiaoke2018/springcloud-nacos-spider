package com.jinguduo.spider.spider.tengxun;

import java.util.List;

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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunPageSpiderIT {

	@Autowired
	private TengxunPageSpider tengxunPageSpider;
	
	@Test
	public void testRun() {
	    String URL = "http://s.video.qq.com/loadplaylist?callback=jQuery19102759763043414536_1465884369999&low_login=1&type=6&id=dxd1v76tmu0wjuj&plname=qq&vtype=3&video_type=10&inorder=1&otype=json&_=1567758140000";
	    Job job = new Job(URL);
	    job.setCode("dxd1v76tmu0wjuj");
	    
	    // request
	    DelayRequest delayRequest = new DelayRequest(job);
	    
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(tengxunPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");
	}
	
	@Test
	public void testRun2() {
	    String URL = "http://s.video.qq.com/loadplaylist?callback=jQuery19102759763043414536_1465884369999&low_login=1&type=6&id=86621&plname=qq&vtype=3&video_type=10&inorder=1&otype=json&_=1567758140000";
	    Job job = new Job(URL);
	    job.setCode("50182");
	    
	    // request
	    DelayRequest delayRequest = new DelayRequest(job);
	    
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(tengxunPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");
	}

	@Test
	public void testExRun()  {
		Job job = new Job("http://s.video.qq.com/loadplaylist?callback=jQuery19102759763043414536_1465884369999&low_login=1&type=6&id=70877&plname=qq&vtype=3&video_type=10&inorder=1&otype=json&_=1465884370000");
		job.setCode("44244");
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(tengxunPageSpider).addPipeline(testPipeline).addRequest(new DelayRequest(job)).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		List<Show> shows = resultItems.get(Show.class.getSimpleName());
		Assert.notEmpty(shows, "Bad");
	}

	@Test
	public void testZONGYI()  {
		Job job = new Job("http://s.video.qq.com/loadplaylist?callback=jQuery19102759763043414536_1465884369999&low_login=1&type=6&id=79752&plname=qq&vtype=3&video_type=10&inorder=1&otype=json&_=1465884370000");
		job.setCode("69895");

		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(tengxunPageSpider).addPipeline(testPipeline).addRequest(new DelayRequest(job)).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");
	}

}
