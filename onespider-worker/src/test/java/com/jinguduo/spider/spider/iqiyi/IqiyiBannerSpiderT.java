package com.jinguduo.spider.spider.iqiyi;

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
import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.webmagic.ResultItems;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiBannerSpiderT {
	
	@Autowired 
	private IqiyiPageSpider pageSpider;
	
	@Autowired 
	private IqiyiMobileSpider mobileSpider;
	
	private static final String entry_url = "https://www.iqiyi.com#WEB_HOME_BANNER";
	private static final String banner_url_type1 = "https://www.iqiyi.com/v_19rw6qb4po.html#MOBILE_HOME_BANNER";
	private static final String banner_url_type2 = "https://www.iqiyi.com/kszt/2020DaVos.html#WEB_HOME_BANNER";
	private static final String banner_url_type3 = "https://www.iqiyi.com/v_19rw526uao.html#WEB_HOME_BANNER";
	private static final String banner_url_type4 = "https://www.iqiyi.com/v_19rw4u1hcw.html#MOBILE_HOME_RECOMMEND";
	private static final String channel_url = "https://www.iqiyi.com/dianshiju/#WEB_CHANNEL_BANNER";
	

	private static final String mobiel_home_url = "https://m.iqiyi.com#MOBILE_HOME_BANNER";

	private static final String mobiel_channel_url = "https://m.iqiyi.com/dianshiju/#MOBILE_CHANNEL_BANNER";
	@Test
	public void testHomeBanner() {
		Job job = new Job();
		job.setUrl(banner_url_type4);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(pageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<BannerRecommendation> banner = resultItems.get(BannerRecommendation.class.getSimpleName());
		
		if(null!=banner)banner.forEach(b->System.out.println(b.toString()));
		
		List<Job> subJobs = resultItems.get(Job.class.getSimpleName());
		
		if(null!=subJobs) subJobs.forEach(j->System.out.println(j.toString()));
	}
	
	@Test
	public void testMobileBanner() {
		Job job = new Job();
		job.setUrl(mobiel_channel_url);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(mobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<BannerRecommendation> banner = resultItems.get(BannerRecommendation.class.getSimpleName());
		
		if(null!=banner)banner.forEach(b->System.out.println(b.toString()));
		
		List<Job> subJobs = resultItems.get(Job.class.getSimpleName());
		
		if(null!=subJobs) subJobs.forEach(j->System.out.println(j.toString()));
		
	}
}
