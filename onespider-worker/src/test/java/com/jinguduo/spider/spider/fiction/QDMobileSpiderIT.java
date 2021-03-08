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
import com.jinguduo.spider.data.table.FictionChapters;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.data.text.FictionCommentText;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class QDMobileSpiderIT {

	@Autowired
	private QDMobileSpider qdMobileSpider;


	@Test
	public void testEntrance() {
		String url = "https://m.qidian.com/majax/rank/signlist?_csrfToken=rLiNuL9peuCr0UsRWjLMMmmL7MO6QBm3Y9Te5cIR&gender=male&pageNum=2&catId=-1";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Job>  rankJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(rankJobs,"rankJobs is empty.");
		System.out.println(rankJobs.size());
		System.out.println(rankJobs.get(0).toString());
		
		job = rankJobs.get(rankJobs.size()-1);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		
		List<Fiction>  fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notEmpty(fictions,"fictions is empty.");
		System.out.println(fictions.get(0).toString());
		
		List<Job>  detailJob = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(detailJob,"detailJob is empty.");
		System.out.println(detailJob.size());
		System.out.println(detailJob.get(0).toString());
		
		job = detailJob.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		
		List<FictionIncomeLogs>  incomeLogs = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		Assert.notEmpty(incomeLogs,"incomeLogs is empty.");
		System.out.println(incomeLogs.toString());
		

		List<Job>  commentJob = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(commentJob,"commentJob is empty.");
		Assert.isTrue(commentJob.size()==1,"commentJob is more than one.");
		System.out.println(commentJob.get(0).toString());
		
		job = commentJob.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		
		List<FictionCommentLogs>  commentLogs = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.notEmpty(commentLogs,"commentLogs is empty.");
		System.out.println(commentLogs.toString());
		
		List<Job>  commentListJob = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(commentListJob,"commentListJob is empty.");
		System.out.println(commentListJob.get(0).toString());
		
		job = commentListJob.get(1);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		
		List<FictionCommentText>  comments = resultItems.get(FictionCommentText.class.getSimpleName());
		Assert.notEmpty(comments,"commentRawText is empty.");
		System.out.println(comments.toString());
	}
	
	@Test
	public void testDetail() {
		String url = "https://m.qidian.com/book/3486912#channel-1";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<FictionIncomeLogs>  incomeLogs = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		Assert.notEmpty(incomeLogs,"incomeLogs is empty.");
		System.out.println(incomeLogs.toString());
		

		List<Job>  commentJob = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(commentJob,"commentJob is empty.");
		Assert.isTrue(commentJob.size()==1,"commentJob is more than one.");
		System.out.println(commentJob.get(0).getUrl());
		
		job = commentJob.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		
		List<FictionCommentLogs>  commentLogs = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.notEmpty(commentLogs,"commentLogs is empty.");
		System.out.println(commentLogs.toString());
	}
	
	@Test 
	public void testCatalog() {
		String url ="https://m.qidian.com/book/3486912/catalog";
		Job job = new Job(url);
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<FictionChapters>  chapters = resultItems.get(FictionChapters.class.getSimpleName());
		Assert.notEmpty(chapters,"chapters is empty.");
		System.out.println(chapters.toString());
	}

	@Test
    public void testMajaxBook() {
        String url = "https://m.qidian.com/majax/forum/getBookForumList?_csrfToken=Kd96RjSl3FVuXRca4kfP7O36CZCKc4QxGuMLH3E1&bookId=1010734492&pageNum=100";
        Job job = new Job(url);
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(qdMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs  = resultItems.get(Job.class.getSimpleName());
        System.out.println("aaa");
	}
}
