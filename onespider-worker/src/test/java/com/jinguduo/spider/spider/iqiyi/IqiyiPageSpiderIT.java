package com.jinguduo.spider.spider.iqiyi;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
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
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiPageSpiderIT {

	@Autowired
	private IqiyiPageSpider iqiyiPageSpider;

	@Test
    public void testAutonFindByBanner()  {
//		无电视剧api  job
        Job job = new Job();
        job.setUrl("http://www.iqiyi.com/dianshiju/");
        job.setCode("206229601");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(iqiyiPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
//        Assert.notEmpty(jobs, "Bad");
//
//        Assert.notNull(jobs.get(0).getCode(), "Bad code");
//        Assert.notNull(jobs.get(0).getUrl(), "Bad url");
//
//        List<Show> shows = resultItems.get(Show.class.getSimpleName());
//        Assert.notEmpty(shows, "Bad");
    }

	@Test
	public void testRun()  {
		Job job = new Job();
		job.setUrl("https://www.iqiyi.com/v_19ry4hpvxo.html");

		job.setCode("208157001");

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(iqiyiPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

        Assert.notNull(jobs.get(0).getCode(), "Bad code");
        Assert.notNull(jobs.get(0).getUrl(), "Bad url");
	}
	
	@Test
    public void testRun2()  {
        Job job = new Job();
        job.setUrl("http://www.iqiyi.com/v_19rr2aesjs.html");
        job.setCode("207834001");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(iqiyiPageSpider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(delayRequest)
            .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");

        Assert.notNull(jobs.get(0).getCode(), "Bad code");
        Assert.notNull(jobs.get(0).getUrl(), "Bad url");
    }
	
	@Test
	public void testNetMovie1()  {
		Job job = new Job();
		job.setUrl("http://www.iqiyi.com/v_19rra1ws80.html#vfrm=2-4-0-1");
		job.setCode("575580300");

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(iqiyiPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

        Assert.notNull(jobs.get(0).getCode(), "Bad code");
        Assert.notNull(jobs.get(0).getUrl(), "Bad url");
	}
	
	/**
	 * 电影入口
	 * @
	 */
	@Test
	public void testFilm()  {
//		原测试用剧已下架
		Job job = new Job();
		job.setUrl("https://www.iqiyi.com/v_19rqw8snpc.html#vfrm=19-9-0-1");
		job.setCode("465139600");

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(iqiyiPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

        Assert.notNull(jobs.get(0).getCode(), "Bad code");
        Assert.notNull(jobs.get(0).getUrl(), "Bad url");
	}
	/**
	 * @
	 */
	@Test
	public void testZongYi()  {
		Job job = new Job();
		job.setUrl("http://www.iqiyi.com/v_19rr9vai4c.html");
		job.setCode("224570001");

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(iqiyiPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

        Assert.notNull(jobs.get(0).getCode(), "Bad code");
        Assert.notNull(jobs.get(0).getUrl(), "Bad url");

	}

	@Test
	public void testAnime()  {
		Job job = new Job();
		job.setUrl("http://www.iqiyi.com/v_19rr8huyr4.html");
		job.setCode("566838");

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(iqiyiPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

        Assert.notNull(jobs.get(0).getCode(), "Bad code");
        Assert.notNull(jobs.get(0).getUrl(), "Bad url");

	}

	@Test
	public void testHome()  {
//		无 process处理
		String homeUrl = "http://www.iqiyi.com";
		Job job = new Job();
		job.setUrl(homeUrl);

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(iqiyiPageSpider)
		    .addPipeline(testPipeline)
		    .addRequest(delayRequest)
		    .setDownloader(new DownloaderManager())
		    .run();

		ResultItems resultItems = testPipeline.getResultItems();
//		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
//		Assert.notEmpty(jobs,"home job not null");

	}
}
