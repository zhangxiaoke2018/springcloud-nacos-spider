package com.jinguduo.spider.spider.iqiyi;


import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiPageSpiderTests {
	
	@Autowired
	private IqiyiPageSpider iqiyiPageSpider;

	@Test
	public void testProcessOk() throws Exception {
		String htmlFile = "/html/iqiyi/IqiyiDetailPageSpiderTests.html";
		String rawText = IoResourceHelper.readResourceContent(htmlFile);
		
		String ALBUM_ID = "422060400";
		String URL = "http://www.iqiyi.com/v_19rrkchixo.html#vfrm=2-4-0-1";
		
		Job job = new Job(URL);
		job.setCode(ALBUM_ID);
		
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(rawText);

		iqiyiPageSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems, "Bad");

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		Assert.notNull(jobs.get(0).getCode(), "Bad");
		Assert.notNull(jobs.get(0).getUrl(),  "Bad");
	}
	
	@Test
	public void testProcessNetMoive1() throws Exception {
		String htmlFile = "/html/iqiyi/netmovie-19rra1ws80-1.html";
		String rawText = IoResourceHelper.readResourceContent(htmlFile);
		
		String code = "575580300";
		String url = "https://www.iqiyi.com/v_19rra1ws80.html#vfrm=2-4-0-1";
		
		Job job = new Job(url);
		job.setCode(code);
		
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(rawText);

		iqiyiPageSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems, "Bad");

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		Assert.notNull(jobs.get(0).getCode(), "Bad");
		Assert.notNull(jobs.get(0).getUrl(),  "Bad");
	}
	
	@Test
	public void testProcessTvDrama1() throws Exception {
		String htmlFile = "/html/iqiyi/tvdrama-19rrhclxdh-1.html";
		String rawText = IoResourceHelper.readResourceContent(htmlFile);
		
		String code = "217914201";
		String url = "https://www.iqiyi.com/a_19rrhclxdh.html#vfrm=2-4-0-1";
		
		Job job = new Job(url);
		job.setCode(code);
		
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(rawText);

		iqiyiPageSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems, "Bad");

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		Assert.notNull(jobs.get(0).getCode(), "Bad");
		Assert.notNull(jobs.get(0).getUrl(),  "Bad");
	}
	
	@Test
	public void testProcessMoive1() throws Exception {
		String htmlFile = "/html/iqiyi/movie-19rr7pc5qg-1.html";
		String rawText = IoResourceHelper.readResourceContent(htmlFile);
		
		String code = "348299500";
		String url = "https://www.iqiyi.com/19rr7pc5qg.html";
		
		Job job = new Job(url);
		job.setCode(code);
		
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(rawText);

		iqiyiPageSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems, "Bad");

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		Assert.notNull(jobs.get(0).getCode(), "Bad");
		Assert.notNull(jobs.get(0).getUrl(),  "Bad");
	}
	
	@Test
	public void testProcessMoive2() throws Exception {
		String htmlFile = "/html/iqiyi/movie-19rrl7h9hs-2.html";
		String rawText = IoResourceHelper.readResourceContent(htmlFile);
		
		String code = "733555300";
		String url = "https://www.iqiyi.com/19rrl7h9hs.html";
		
		Job job = new Job(url);
		job.setCode(code);
		
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(rawText);

		iqiyiPageSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems, "Bad");

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		Assert.notNull(jobs.get(0).getCode(), "Bad");
		Assert.notNull(jobs.get(0).getUrl(),  "Bad");
	}
	
	@Test
	public void testProcessMoive3() throws Exception {
		String htmlFile = "/html/iqiyi/movie-19rro1vdkw-3.html";
		String rawText = IoResourceHelper.readResourceContent(htmlFile);
		
		String code = "348299500";
		String url = "https://www.iqiyi.com/v_19rro1vdkw.html";
		
		Job job = new Job(url);
		job.setCode(code);
		
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(rawText);

		iqiyiPageSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems, "Bad");

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		Assert.notNull(jobs.get(0).getCode(), "Bad");
		Assert.notNull(jobs.get(0).getUrl(),  "Bad");
	}
	
	@Test
	public void testTotalPlayCountJob() throws Exception {
		String htmlFile = "/html/iqiyi/movie-19rr7pc5qg-1.html";
		String rawText = IoResourceHelper.readResourceContent(htmlFile);
		
		String code = "733555300";
		String url = "https://www.iqiyi.com/v_19rr7pc5qg.html#vfrm=2-4-0-1";
		
		Job job = new Job(url);
		job.setCode(code);
		
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(rawText);

		iqiyiPageSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems, "Bad");

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		Assert.notEmpty(jobs, "Bad");

		Assert.notNull(jobs.get(0).getCode(), "Bad");
		Assert.notNull(jobs.get(0).getUrl(),  "Bad");
	}
}
