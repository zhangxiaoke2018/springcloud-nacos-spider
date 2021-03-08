package com.jinguduo.spider.spider.youku;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuPageSpiderT {
	@Autowired 
	private YoukuPageSpider pageSpider;
	
	@Autowired 
	private YoukuBannerSpider showPageSpider;
	
	
	private static final String entry_url = "https://www.youku.com#WEB_HOME_BANNER";
	

	private static final String channel_url = "https://tv.youku.com#WEB_CHANNEL_BANNER";

	private static final String mobile_url = "https://www.youku.com#MOBILE_HOME_BANNER";
	
	private static final String mobile_channel_url = "https://tv.youku.com#MOBILE_CHANNEL_BANNER";
	
	
	@Test
	public void testHomeBanner() {
		Job job = new Job();
		job.setUrl(entry_url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(pageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<BannerRecommendation> banner = resultItems.get(BannerRecommendation.class.getSimpleName());
		
		if(null!=banner)banner.forEach(b->System.out.println(b.toString()));

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		
		if(null!=jobs)jobs.forEach(b->System.out.println(b.toString()));
	}
	
	@Test
	public void testChannelBanner() {
		Job job = new Job();
		job.setUrl(channel_url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(showPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<BannerRecommendation> banner = resultItems.get(BannerRecommendation.class.getSimpleName());
		
		if(null!=banner)banner.forEach(b->System.out.println(b.toString()));
		
	}
	
	@Test
	public void testMobileBanner() {
		Job job = new Job();
		job.setUrl(mobile_url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(pageSpider).addPipeline(testPipeline)
		.setSpiderListeners(Lists.asList(new YoukuBannerSpiderListener(),new YoukuBannerSpiderListener[0]))
				.addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<BannerRecommendation> banner = resultItems.get(BannerRecommendation.class.getSimpleName());
		
		if(null!=banner)banner.forEach(b->System.out.println(b.toString()));
	}
	
	@Test
	public void testMobileChannelBanner() {
		Job job = new Job();
		job.setUrl(mobile_channel_url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(showPageSpider).addPipeline(testPipeline)
		.setSpiderListeners(Lists.asList(new YoukuBannerSpiderListener(),new YoukuBannerSpiderListener[0]))
				.addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<BannerRecommendation> banner = resultItems.get(BannerRecommendation.class.getSimpleName());
		
		if(null!=banner)banner.forEach(b->System.out.println(b.toString()));
	}
}
