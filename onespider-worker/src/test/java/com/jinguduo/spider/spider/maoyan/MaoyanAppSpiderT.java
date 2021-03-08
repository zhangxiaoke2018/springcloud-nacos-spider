package com.jinguduo.spider.spider.maoyan;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.MaoyanActor;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class MaoyanAppSpiderT {
	@Autowired
	private MaoyanAppSpider maoyanAppSpider;

	@Test
	public void testList() {
		String url = "http://piaofang.maoyan.com/celebrity-board/query?page=1&cLevel1Id=29&cLevel2Id=30&cId=1&work-type=-1&work-style=-1&verified=-1&gender=-1";
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setPlatformId(46);
		job.setUrl(url);
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(maoyanAppSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.assertNotNull(jobs);
		System.out.println(jobs.get(0).toString());
	}
	@Test
	public void testDetail() {
		String url = "http://piaofang.maoyan.com/celebrity?id=2153426";
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setPlatformId(46);
		job.setUrl(url);
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(maoyanAppSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		List<MaoyanActor> actors = resultItems.get(MaoyanActor.class.getSimpleName());
		Assert.assertNotNull(actors);
		System.out.println(actors.get(0).toString());
		
	}
}
