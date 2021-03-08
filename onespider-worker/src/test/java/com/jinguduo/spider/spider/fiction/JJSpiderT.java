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
import com.jinguduo.spider.data.text.FictionCommentText;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class JJSpiderT {

	@Autowired
	private JJSpider jjSpider;
//	@Autowired
//	private JJCommentSpider jjCommentSpiderSpider;

	@Test
	public void testEntrance() {
		String url = "http://android.jjwxc.net";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(jjSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> detailJob = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(detailJob, "detailJob is empty.");
		System.out.println(detailJob.size());
		System.out.println(detailJob.get(0).toString());

		job = detailJob.get(0);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(jjSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();

		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notEmpty(fictions, "fictions is empty.");
		System.out.println(fictions.size());
		System.out.println(fictions.get(0).toString());

		List<FictionCommentLogs> commentLogs = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.notEmpty(commentLogs, "commentLogs is empty.");
		System.out.println(commentLogs.toString());

		List<FictionIncomeLogs> incomeLogs = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		Assert.notEmpty(incomeLogs, "incomeLogs is empty.");
		System.out.println(incomeLogs.toString());
	}

	@Test
	public void testDetail() {
		String url = "http://android.jjwxc.net/androidapi/novelbasicinfo?novelId=3194253";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(jjSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notEmpty(fictions, "fictions is empty.");
		System.out.println(fictions.size());
		System.out.println(fictions.get(0).toString());

		List<FictionCommentLogs> commentLogs = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.notEmpty(commentLogs, "commentLogs is empty.");
		System.out.println(commentLogs.toString());

		List<FictionIncomeLogs> incomeLogs = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		Assert.notEmpty(incomeLogs, "incomeLogs is empty.");
		System.out.println(incomeLogs.toString());
	}



}
