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
public class MTSpiderT {

	@Autowired
	private MTMobileSpider motieMobileSpider;
	@Autowired
	private MTWebSpider motieWebSpider;

	@Test
	public void test() {
		String url = "http://app.motie.com";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(motieMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());

		Assert.notNull(jobs, "rank  job not null");
		System.out.println("rank all job =" + jobs.size());
		System.out.println(jobs.get(1).toString());

		job = jobs.get(1);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(motieMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();

		jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notNull(jobs, "rank job not null");
		System.out.println("rank job =" + jobs.size());
		System.out.println(jobs.get(0).toString());

		job = jobs.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(motieMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();

		jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notNull(jobs, "detail job not null");
		System.out.println("detail and income job =" + jobs.size());
		System.out.println(jobs.get(1).toString());

		job = jobs.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(motieMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();

		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notNull(fictions, "fictions not null");
		System.out.println(fictions.get(0).toString());

		List<FictionCommentLogs> commentLogs = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.notNull(commentLogs, "comment not null");
		System.out.println(commentLogs.get(0).toString());

	}

	@Test
	public void testIncome() {
		String url = "http://www.motie.com/book/100161/donate/list";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(motieWebSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		List<FictionIncomeLogs> incomeLogs = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		Assert.isTrue(incomeLogs.size()==1,"can't get income");
		System.out.println(incomeLogs.get(0).toString());
	}
}
