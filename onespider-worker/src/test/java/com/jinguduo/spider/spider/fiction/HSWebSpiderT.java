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
public class HSWebSpiderT {
	@Autowired
	private HSWebSpider spider;

	@Test
	public void testEntrance() {
		String url = "http://www.hongshu.com";

		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(new DelayRequest(new Job(url))).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notEmpty(fictions, "fictions can not be empty");
		System.out.println("size=" + fictions.size() + ":" + fictions.get(15).toString());

		List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(detailJobs, "detail job can not be empty");
		System.out.println("size=" + detailJobs.size() + ":" + detailJobs.get(15).toString());
		
		testPipeline = new TestPipeline();
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(new DelayRequest(detailJobs.get(15))).run();
		resultItems = testPipeline.getResultItems();
		
		List<FictionIncomeLogs> incomes = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		System.out.println(incomes.get(0));
		
		List<FictionCommentLogs> comments = resultItems.get(FictionCommentLogs.class.getSimpleName());
		System.out.println(comments.get(0));
	}
}
