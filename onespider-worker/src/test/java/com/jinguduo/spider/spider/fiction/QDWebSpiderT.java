package com.jinguduo.spider.spider.fiction;

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
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.data.table.FictionPlatformRecommend;
import com.jinguduo.spider.webmagic.ResultItems;
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class QDWebSpiderT {
	@Autowired
	private QDWebSpider spider;

	@Test
	public void testDetail() {
		String url = "https://book.qidian.com/info/1013068636";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
//		List<FictionPlatformClick>  clickLogs = resultItems.get(FictionPlatformClick.class.getSimpleName());
//		clickLogs.forEach(System.out::println);
		List<FictionPlatformRecommend>  recommend = resultItems.get(FictionPlatformRecommend.class.getSimpleName());
		recommend.forEach(System.out::println);
		
		List<Job>  scoreJob = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(scoreJob,"scoreJob is empty.");
		
		job = scoreJob.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		List<FictionPlatformRate> rate = resultItems.get(FictionPlatformRate.class.getSimpleName());
		rate.forEach(System.out::println);
	}
	
}
