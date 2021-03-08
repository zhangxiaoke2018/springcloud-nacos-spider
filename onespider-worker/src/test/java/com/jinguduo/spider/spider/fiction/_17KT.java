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
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformFavorite;
import com.jinguduo.spider.data.table.FictionPlatformRecommend;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class _17KT {

	@Autowired
	private _17KH5Spider h5Spider;


	@Test
	public void testEntrance() {
		String url = "https://h5.17k.com";
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(new Job(url));
		SpiderEngine.create(h5Spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> rankJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(rankJobs, "rank job is empty");
		System.out.println("rank job size=" + rankJobs.size() + " first job=" + rankJobs.get(0).toString());

		testPipeline = new TestPipeline();
		delayRequest = new DelayRequest(rankJobs.get(0));
		SpiderEngine.create(h5Spider).addPipeline(testPipeline)
				.addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(detailJobs, "detail job is empty");
		System.out.println("detail job size=" + detailJobs.size() + " first job=" + detailJobs.get(0).toString());

		testPipeline = new TestPipeline();
		delayRequest = new DelayRequest(detailJobs.get(0));
		SpiderEngine.create(h5Spider).addPipeline(testPipeline)
				.addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		
		List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notEmpty(fictions, "fictions of rank list should not be empty.");
		System.out.println("ficiton =" + fictions.get(0).toString());

		testPipeline = new TestPipeline();
		delayRequest = new DelayRequest(detailJobs.get(1));
		SpiderEngine.create(h5Spider).addPipeline(testPipeline)
				.addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		List<FictionCommentLogs> comment = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.notEmpty(comment, "comment should not be empty.");
		System.out.println("comment =" + comment.get(0).toString());
		
		List<FictionIncomeLogs> income = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		Assert.notEmpty(comment, "income should not be empty.");
		System.out.println("income =" + income.get(0).toString());
		
		List<FictionPlatformClick> click = resultItems.get(FictionPlatformClick.class.getSimpleName());
		Assert.notEmpty(click, "click should not be empty.");
		System.out.println("click =" + click.get(0).toString());
		
		List<FictionPlatformFavorite> favorite = resultItems.get(FictionPlatformFavorite.class.getSimpleName());
		Assert.notEmpty(favorite, "favorite should not be empty.");
		System.out.println("favorite =" + favorite.get(0).toString());
		
		List<FictionPlatformRecommend> recommend = resultItems.get(FictionPlatformRecommend.class.getSimpleName());
		Assert.notEmpty(recommend, "recommend should not be empty.");
		System.out.println("recommend =" + recommend.get(0).toString());

	}
}
