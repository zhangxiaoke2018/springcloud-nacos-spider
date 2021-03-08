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
import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class Xiang5SpiderT {

	@Autowired
	private Xiang5Spider spider;
	
	@Test
	public void testEntrance(){
		String url = "http://www.xiang5.com";
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(new DelayRequest(new Job(url))).run();
		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> rankJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(rankJobs,"rank job can not be empty");
		System.out.println("size="+rankJobs.size()+":"+rankJobs.get(0).toString());

		testPipeline = new TestPipeline();
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(new DelayRequest(rankJobs.get(0))).run();
		resultItems = testPipeline.getResultItems();
		List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(detailJobs,"detail job can not be empty");
		System.out.println("size="+detailJobs.size()+":"+detailJobs.get(0).toString());
		
		testPipeline = new TestPipeline();
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(new DelayRequest(detailJobs.get(0))).run();
		resultItems = testPipeline.getResultItems();
		
		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		System.out.println(fictions.get(0));
		List<FictionIncomeLogs> incomes = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		System.out.println(incomes.get(0));
		
		List<FictionCommentLogs> comments = resultItems.get(FictionCommentLogs.class.getSimpleName());
		System.out.println(comments.get(0));
		
		
	}

	
	@Test
	public void testDetail(){
		String url="http://www.xiang5.com/bookinfo/101260.html";
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job(url);
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		
		ResultItems resultItems =  testPipeline.getResultItems();
		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		System.out.println(fictions.get(0));
		List<FictionIncomeLogs> incomes = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		System.out.println(incomes.get(0));
		
		List<FictionCommentLogs> comments = resultItems.get(FictionCommentLogs.class.getSimpleName());
		System.out.println(comments.get(0));
		
	}
}
