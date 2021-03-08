package com.jinguduo.spider.spider.fiction;

import java.util.List;

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
import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformFavorite;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IReaderSpiderT {
	@Autowired
	private IReaderSpider iReaderSpider;

	@Autowired
	private IReaderWebSpider iReaderWebSpider;

	private String urlEntrance = "http://ah2.zhangyue.com";
	private String urlCategoryEntrance = "http://ah2.zhangyue.com/zybk/api/category/category";

	@Test
	public void test() {
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine engine = SpiderEngine.create(iReaderSpider).addPipeline(testPipeline);

		engine.addRequest(new DelayRequest(new Job(urlEntrance))).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> rankJobs = resultItems.get(Job.class.getSimpleName());
//		for (Job i : rankJobs) {
////			System.out.println("size=" + rankJobs.size() + ":" + rankJobs.get(15).toString());
//
//			engine.addRequest(new DelayRequest(i)).run();
//			resultItems = testPipeline.getResultItems();
//			List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
//			for (Job j : detailJobs) {
//				// System.out.println("size=" + detailJobs.size() + ":" +
//				// detailJobs.get(0).toString());
//
//				engine.addRequest(new DelayRequest(j)).run();
//				resultItems = testPipeline.getResultItems();
//			}
//		}
//		List<Fiction> detail = resultItems.get(Fiction.class.getSimpleName());
//		System.out.println(detail.get(0).toString());
//
//		List<FictionPlatformClick> click = resultItems.get(FictionPlatformClick.class.getSimpleName());
//		System.out.println(click.get(0).toString());
//
//		List<FictionPlatformFavorite> favorite = resultItems.get(FictionPlatformFavorite.class.getSimpleName());
//		System.out.println(favorite.get(0).toString());
//
//		List<FictionCommentLogs> comment = resultItems.get(FictionCommentLogs.class.getSimpleName());
//		System.out.println(comment.get(0).toString());
	}

	@Test
	public void testCategory() {
//		TestPipeline testPipeline = new TestPipeline();
//		SpiderEngine engine = SpiderEngine.create(iReaderSpider).addPipeline(testPipeline);
//
//		engine.addRequest(new DelayRequest(new Job(urlCategoryEntrance))).run();
//
//		ResultItems resultItems = testPipeline.getResultItems();
//		List<Job> rankJobs = resultItems.get(Job.class.getSimpleName());
		
		//System.out.println("size=" + rankJobs.size() + ":" + rankJobs.get(0).toString());
//		for (Job i : rankJobs) {
//			engine.addRequest(new DelayRequest(i)).run();
//			resultItems = testPipeline.getResultItems();
//			List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
//			for(Job j:detailJobs) {
//			engine.addRequest(new DelayRequest(j)).run();
//			resultItems = testPipeline.getResultItems();
//			}
//		}
//		List<Fiction> detail = resultItems.get(Fiction.class.getSimpleName());
//		System.out.println(detail.get(0).toString());
//
//		List<FictionPlatformClick> click = resultItems.get(FictionPlatformClick.class.getSimpleName());
//		System.out.println(click.get(0).toString());
//
//		List<FictionPlatformFavorite> favorite = resultItems.get(FictionPlatformFavorite.class.getSimpleName());
//		System.out.println(favorite.get(0).toString());
//
//		List<FictionCommentLogs> comment = resultItems.get(FictionCommentLogs.class.getSimpleName());
//		System.out.println(comment.get(0).toString());
	}

	@Test
	public void testScore() {
		String url = "http://www.ireader.com/index.php?ca=bookdetail.index&pca=booksort.index&bid=11761134";
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine engine = SpiderEngine.create(iReaderWebSpider).addPipeline(testPipeline);

		engine.addRequest(new DelayRequest(new Job(url))).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<FictionPlatformRate> rate = resultItems.get(FictionPlatformRate.class.getSimpleName());
		System.out.println(rate.get(0).toString());

	}
}
