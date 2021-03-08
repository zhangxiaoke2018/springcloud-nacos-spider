package com.jinguduo.spider.spider.fiction;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformFavorite;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class QQReaderSpiderT {
	@Autowired
	private QQReaderSpider aReaderSpider;
	
	private String url_entrance = "http://newandroid.reader.qq.com/v6_6_6/listDispatch";
	private String urlCategoryEntrance = "http://newandroid.reader.qq.com/v6_6_6/queryOperation?categoryFlag=2";
	
	@Test
	public void test() {
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine engine = SpiderEngine.create(aReaderSpider).addPipeline(testPipeline)
		.addSpiderListeners(ImmutableList.of(new QQReaderHeaderListener()));
		
		engine.addRequest(new DelayRequest(new Job(url_entrance))).run();
		
		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> rankJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(rankJobs,"rank job can not be empty");
		System.out.println("size="+rankJobs.size()+":"+rankJobs.get(0).toString());
		
		engine.addRequest(new DelayRequest(rankJobs.get(0))).run();
		resultItems = testPipeline.getResultItems();
		List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
		System.out.println("size="+detailJobs.size()+":"+detailJobs.get(0).toString());
		
		engine.addRequest(new DelayRequest(detailJobs.get(0))).run();
		resultItems = testPipeline.getResultItems();
		List<Fiction> detail = resultItems.get(Fiction.class.getSimpleName());
		System.out.println(detail.get(0).toString());
		
		List<FictionPlatformRate> rate = resultItems.get(FictionPlatformRate.class.getSimpleName());
		System.out.println(rate.get(0).toString());
		
		List<FictionPlatformClick> click = resultItems.get(FictionPlatformClick.class.getSimpleName());
		System.out.println(click.get(0).toString());
		

		List<FictionPlatformFavorite> fav = resultItems.get(FictionPlatformFavorite.class.getSimpleName());
		System.out.println(fav.get(0).toString());
		
		List<FictionCommentLogs> comment = resultItems.get(FictionCommentLogs.class.getSimpleName());
		System.out.println(comment.get(0).toString());
	}
	
	@Test
	public void testCategory() {
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine engine = SpiderEngine.create(aReaderSpider).addPipeline(testPipeline)
				.addSpiderListeners(ImmutableList.of(new QQReaderHeaderListener()));
		
		engine.addRequest(new DelayRequest(new Job(urlCategoryEntrance))).run();
		
		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> rankJobs = resultItems.get(Job.class.getSimpleName());
		System.out.println("size="+rankJobs.size()+":"+rankJobs.get(1).toString());
		
		engine.addRequest(new DelayRequest(rankJobs.get(1))).run();
		resultItems = testPipeline.getResultItems();
		List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
		System.out.println("size="+detailJobs.size()+":"+detailJobs.get(0).toString());
		
		engine.addRequest(new DelayRequest(detailJobs.get(0))).run();
		resultItems = testPipeline.getResultItems();
		
		List<Fiction> detail = resultItems.get(Fiction.class.getSimpleName());
		System.out.println(detail.get(0).toString());
		
		List<FictionPlatformRate> rate = resultItems.get(FictionPlatformRate.class.getSimpleName());
		System.out.println(rate.get(0).toString());
		
		List<FictionPlatformClick> click = resultItems.get(FictionPlatformClick.class.getSimpleName());
		System.out.println(click.get(0).toString());
		
		List<FictionPlatformFavorite> fav = resultItems.get(FictionPlatformFavorite.class.getSimpleName());
		System.out.println(fav.get(0).toString());
		
		List<FictionCommentLogs> comment = resultItems.get(FictionCommentLogs.class.getSimpleName());
		System.out.println(comment.get(0).toString());
	}
}
