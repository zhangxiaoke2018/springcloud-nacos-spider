package com.jinguduo.spider.spider.fiction;

import java.util.List;

import com.jinguduo.spider.data.table.*;
import org.apache.commons.collections.CollectionUtils;
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
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ZHSpiderT {

	@Autowired
	private ZHMobileSpider zhAppSpider;

	@Autowired
	private ZHApiSpider zhApiSpider;
	
	@Autowired
	private ZHWebSpider zhWebSpider;
	@Autowired
	ZHPcSpider zhPcSpider;

	@Test


	public void testPC(){
		String url = "http://www.zongheng.com/rank/details.html?rt=1&d=1&i=2&p=2";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(zhPcSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

	}

	@Test
	public void testEntrance() {
		String url = "https://m.zongheng.com";

		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(zhAppSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());

		Assert.notEmpty(jobs, "rank jobs is empty");
		System.out.println(jobs.size());
		System.out.println(jobs.get(0).toString());

		job = jobs.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(zhAppSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();

		jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "detail jobs is empty");
		System.out.println(jobs.size());
		System.out.println(jobs.get(0).toString());

		job = jobs.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(zhApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();

		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notEmpty(fictions, "fiction is empty");
		System.out.println(fictions.size());
		System.out.println(fictions.get(0).toString());

		jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "extra info jobs is empty");
		System.out.println(jobs.size());
		System.out.println(jobs.get(0).toString());

		job = jobs.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(zhApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();

		List<FictionIncomeLogs> infoLogs = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		List<FictionPlatformClick> click = resultItems.get(FictionPlatformClick.class.getSimpleName());
		List<FictionPlatformRecommend> recommend = resultItems.get(FictionPlatformRecommend.class.getSimpleName());
		if (CollectionUtils.isNotEmpty(infoLogs))
			System.out.println(infoLogs.get(0).toString());
		System.out.println(click.get(0).toString());
		System.out.println(recommend.get(0).toString());
		jobs = resultItems.get(Job.class.getSimpleName());
		Assert.isTrue(jobs.size()==1,"comment job more than one");
		System.out.println(jobs.get(0).toString());
		
		job = jobs.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(zhApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		List<FictionCommentLogs> commentLogs = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.isTrue(commentLogs.size()==1,"comment log more than one");
		System.out.println(commentLogs.get(0).toString());
	}
	
	@Test
	public void testChapter() {
		String url = "http://book.zongheng.com/showchapter/672340.html";
		String url2 = "https://m.zongheng.com/h5/ajax/chapter/list?h5=1&bookId=259018&pageNum=1&pageSize=100";
		Job job = new Job(url2);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(zhAppSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		List<FictionChapters> chapters = resultItems.get(FictionChapters.class.getSimpleName());
		System.out.println(chapters.get(0));
	}
}
