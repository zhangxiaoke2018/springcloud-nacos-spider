package com.jinguduo.spider.spider.douban;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.common.proxy.FastProxyPool;
import com.jinguduo.spider.data.table.DouBanActor;
import com.jinguduo.spider.data.table.DoubanCommentsText;
import com.jinguduo.spider.data.table.DoubanLog;
import com.jinguduo.spider.data.table.ShowActors;
import com.jinguduo.spider.service.QiniuService;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DoubanMovieSpiderIT {

	@Autowired
	private DoubanMovieSpider doubanMovieSpider;

	@Autowired
	private DoubanMobileSpider doubanMobileSpider;
	
	final static String DOUBAN_MOVIE_URL_1 = "https://movie.douban.com/subject/27185556/#gd-showname=298f26305dde10a1d2e3eafd4f0967ab";
	final static String DOUBAN_MOVIE_URL_2 = "https://movie.douban.com/subject/25931446/"; // 电视剧
	final static String DOUBAN_MOVIE_URL_3 = "https://movie.douban.com/subject/26939233/"; // 电影
	final static String DOUBAN_MOVIE_URL_4 = "https://movie.douban.com/subject/26757373/"; //
	final static String DOUBAN_MOVIE_URL_5 = "https://movie.douban.com/subject/26344996/"; //

	// 3个ul
	final static String celebrities_url = "https://movie.douban.com/subject/26363186/celebrities";
	// 1个ul
	final static String celebrities_url2 = "https://movie.douban.com/subject/27598254/celebrities";

	@Mock
	private QiniuService qiniuService;

	@Before
	public void setup() throws FileNotFoundException {
		MockitoAnnotations.initMocks(this);
		Mockito.when(qiniuService.upload(Mockito.anyString())).thenReturn("http://www.xxxxxx.com");
	}

	@Test
	public void testContext() {
		Assert.notNull(doubanMovieSpider);
	}

	@Test
	public void testThemePage() {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setUrl(DOUBAN_MOVIE_URL_5);
		job.setCode("26728159");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		Request request = resultItems.getRequest();
		Assert.isTrue(DOUBAN_MOVIE_URL_5.equals(request.getUrl()));
		List<DoubanLog> doubanLogs = resultItems.get(DoubanLog.class.getSimpleName());
		Assert.notNull(doubanLogs);
		Assert.isTrue(doubanLogs.get(0).getBriefComment() >= 0);
		Assert.isTrue(doubanLogs.get(0).getDiscussionCount() >= 0);
		Assert.isTrue(doubanLogs.get(0).getJudgerCount() >= 0);
		Assert.isTrue(doubanLogs.get(0).getReviewCount() >= 0);

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		for (Job j : jobs) {
			System.out.println(j.getUrl());
			if (j.getUrl().contains("interests")) {
				DelayRequest delayRequest1 = new DelayRequest(j);
				TestPipeline testPipeline1 = new TestPipeline();
				SpiderEngine.create(doubanMobileSpider).addPipeline(testPipeline1).addRequest(delayRequest1).run();
				ResultItems resultItems1 = testPipeline1.getResultItems();
				List<DoubanCommentsText> logs = resultItems1.get(DoubanCommentsText.class.getSimpleName());
				System.out.println(logs.get(0));
			}
		}

	}

	@Test
	public void testThemePage2() {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setUrl(DOUBAN_MOVIE_URL_2);
		job.setCode("25931446");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(doubanMovieSpider).setDownloader(new DownloaderManager()).addPipeline(testPipeline)
				.addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		Request request = resultItems.getRequest();
		Assert.isTrue(DOUBAN_MOVIE_URL_2.equals(request.getUrl()));
		List<DoubanLog> doubanLogs = resultItems.get(DoubanLog.class.getSimpleName());
		Assert.notNull(doubanLogs);
		Assert.isTrue(doubanLogs.get(0).getBriefComment() >= 0);
		Assert.isTrue(doubanLogs.get(0).getDiscussionCount() >= 0);
		Assert.isTrue(doubanLogs.get(0).getJudgerCount() >= 0);
		Assert.isTrue(doubanLogs.get(0).getReviewCount() >= 0);
	}

	@Ignore("handwork")
	@Test
	public void testCookieAndProxy() {
		TestPipeline testPipeline = new TestPipeline();

		Job job1 = new Job();
		job1.setUrl(DOUBAN_MOVIE_URL_1);
		job1.setCode("26728159");
		DelayRequest delayRequest1 = new DelayRequest(job1);

		Job job2 = new Job();
		job2.setUrl(DOUBAN_MOVIE_URL_2);
		job2.setCode("26776472");
		DelayRequest delayRequest2 = new DelayRequest(job2);

		Job job3 = new Job();
		job3.setUrl(DOUBAN_MOVIE_URL_3);
		job3.setCode("26785709");
		DelayRequest delayRequest3 = new DelayRequest(job3);

		Job job4 = new Job();
		job4.setUrl(DOUBAN_MOVIE_URL_4);
		job4.setCode("26757373");
		DelayRequest delayRequest4 = new DelayRequest(job4);

		Job job5 = new Job();
		job5.setUrl(DOUBAN_MOVIE_URL_5);
		job5.setCode("26732535");
		DelayRequest delayRequest5 = new DelayRequest(job5);

		FastProxyPool proxyPool = new FastProxyPool();

		Site site = doubanMovieSpider.getSite();
		site.setProxyPool(proxyPool);
		site.setUserAgent(
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/602.2.14 (KHTML, like Gecko) Version/10.0.1 Safari/602.2.14");
		site.addCookie("douban.com", "ll", "0");
		site.addCookie("douban.com", "bid", "SDjiSSpvM20");

		/*
		 * HttpProxy httpProxy = new HttpProxy();
		 * httpProxy.setHost("211.154.8.56:8888"); proxyPool.addProxy(httpProxy);
		 */

		SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).setDownloader(new DownloaderManager())
				.addRequest(delayRequest1).addRequest(delayRequest2).addRequest(delayRequest3).addRequest(delayRequest4)
				.addRequest(delayRequest5).thread(2).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<DoubanLog> doubanLogs = resultItems.get(DoubanLog.class.getSimpleName());
		Assert.notEmpty(doubanLogs, "bad");
	}

	@Test
	public void testThemePage3() {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setUrl(DOUBAN_MOVIE_URL_3);
		job.setCode("26939233");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		Request request = resultItems.getRequest();
		Assert.isTrue(DOUBAN_MOVIE_URL_3.equals(request.getUrl()));
		List<DoubanLog> doubanLogs = resultItems.get(DoubanLog.class.getSimpleName());
		Assert.notNull(doubanLogs);
		Assert.isTrue(doubanLogs.get(0).getBriefComment() >= 0);
		Assert.isTrue(doubanLogs.get(0).getDiscussionCount() >= 0);
		Assert.isTrue(doubanLogs.get(0).getJudgerCount() >= 0);
		Assert.isTrue(doubanLogs.get(0).getReviewCount() >= 0);
	}

	@Test
	public void testActorInfo() {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setUrl("https://movie.douban.com/celebrity/1320509/");
		job.setCode("1320509");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<DouBanActor> douBanActors = resultItems.get(DouBanActor.class.getSimpleName());
		Assert.notNull(douBanActors);
		Assert.notNull(douBanActors.get(0).getName());
	}

	@Test
	public void testActorInfoImdb() {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setUrl("https://movie.douban.com/celebrity/1023718/");
		job.setCode("1023718");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<DouBanActor> douBanActors = resultItems.get(DouBanActor.class.getSimpleName());
		Assert.notEmpty(douBanActors, "Bad");
		Assert.notNull(douBanActors.get(0).getImdbNumber(), "Bad");
	}

	private final static String COMMENT_NEW_SCORE_URL = "https://movie.douban.com/subject/26614088/comments?sort=new_score";// +
																															// "&status=P&start=0&limit=20"
	private final static String STATUS_PAGIN_URL = "https://movie.douban.com/subject/26614088/comments?status=P";

	/**
	 * 批量生成短评文本任务 @
	 */
	@Test
	public void testPagin() {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		job.setUrl(STATUS_PAGIN_URL);
		job.setCode("26614088");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> jobs = resultItems.get("Job");
		List<DoubanCommentsText> doubanCommentsTexts = resultItems.get(DoubanCommentsText.class.getSimpleName());
		Assert.isTrue(doubanCommentsTexts.size() > 0);
		Assert.isTrue(jobs.size() > 0);
	}

	/**
	 * 解析短评文本 @
	 */
	// @Test
	// public void testComment() {
	// TestPipeline testPipeline = new TestPipeline();
	// Job job = new Job();
	// job.setUrl(COMMENT_NEW_SCORE_URL);
	// job.setCode("26614088");
	// DelayRequest delayRequest = new DelayRequest(job);
	// SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
	// ResultItems resultItems = testPipeline.getResultItems();
	// List<DoubanCommentsText> doubanCommentsTexts =
	// resultItems.get(DoubanCommentsText.class.getSimpleName());
	// Assert.isTrue(doubanCommentsTexts.size()>0);
	// }

	/**
	 * yany @
	 */
	@Test
	public void testCelebrities() {
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job();
		String url = "https://movie.douban.com/subject/27598254/celebrities";
		job.setUrl(url);
		job.setCode("27598254");
		DelayRequest delayRequest = new DelayRequest(job);
		SpiderEngine.create(doubanMovieSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();
		List<ShowActors> showActors = resultItems.get(ShowActors.class.getSimpleName());
		Assert.notEmpty(showActors);
	}
}
