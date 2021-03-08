package com.jinguduo.spider.spider.fiction;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.util.Md5Util;
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
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformFavorite;
import com.jinguduo.spider.data.text.FictionCommentText;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class JJSpiderWebT {

	@Autowired
	private JJSpider jjSpider;
	
	@Autowired
	private JJWebSpider jjWebSpider;

	@Autowired
	private JJMobileSpider mobileSpider;

	@Test
	public void testM(){
		String url = "https://m.jjwxc.net/ranks/vipgold/yq";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(mobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
	}


	@Test
	public void testEntrance() {
		String url = "http://www.jjwxc.net";
		Job job = new Job(url);
		job.setPlatformId(37);
		job.setCode(Md5Util.getMd5(url));
		job.setKind(JobKind.Once);
		System.out.println(JSONObject.toJSONString(job));


		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(jjWebSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Job>  rankJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(rankJobs,"rank is empty.");
		System.out.println(rankJobs.size());
		System.out.println(rankJobs.get(0).toString());

		job = rankJobs.stream().filter(p -> p.getUrl().contains("orderstr=3&t=0")).findFirst().orElse(null);
		//job = rankJobs.get(5);
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(jjWebSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		

		List<Job>  detailJobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(detailJobs,"detail is empty.");
		System.out.println(detailJobs.size());
		System.out.println(detailJobs.get(10).toString());
		
		job = detailJobs.get(10);
		job.setUrl("http://android.jjwxc.net/androidapi/novelbasicinfo?novelId=3403679");
		delayRequest = new DelayRequest(job);
		testPipeline = new TestPipeline();
		SpiderEngine.create(jjSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		
		
		List<Fiction>  fictions = resultItems.get(Fiction.class.getSimpleName());
		Assert.notEmpty(fictions,"fictions is empty.");
		System.out.println(fictions.size());
		System.out.println(fictions.get(0).toString());
		
		List<FictionCommentLogs>  commentLogs = resultItems.get(FictionCommentLogs.class.getSimpleName());
		Assert.notEmpty(commentLogs,"commentLogs is empty.");
		System.out.println(commentLogs.toString());
		
		List<FictionIncomeLogs>  incomeLogs = resultItems.get(FictionIncomeLogs.class.getSimpleName());
		Assert.notEmpty(incomeLogs,"incomeLogs is empty.");
		System.out.println(incomeLogs.toString());
		
		List<FictionPlatformClick>  clicks = resultItems.get(FictionPlatformClick.class.getSimpleName());
		Assert.notEmpty(clicks,"clicks is empty.");
		System.out.println(clicks.toString());
		
		List<FictionPlatformFavorite>  favorites = resultItems.get(FictionPlatformFavorite.class.getSimpleName());
		Assert.notEmpty(favorites,"favorites is empty.");
		System.out.println(favorites.toString());
		
		List<FictionChapters>  chapterInfos = resultItems.get(FictionChapters.class.getSimpleName());
		Assert.notEmpty(chapterInfos,"chapterInfo is empty.");
		System.out.println(chapterInfos.toString());
	}
	
	@Test
	public void testCommentText() {
		String url = "http://www.jjwxc.net/comment.php?novelid=3142278&chapterid=68&page=56";
		Job job = new Job(url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(jjWebSpider).addPipeline(testPipeline)
		.addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
//		List<List<FictionCommentText>> list = resultItems.get(List.class.getSimpleName());
//		System.out.println(list.get(0).get(0));

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		System.out.println(jobs.get(0));
	}
}
