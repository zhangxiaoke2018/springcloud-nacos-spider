package com.jinguduo.spider.spider.tengxun;

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
public class TengxunMobileSpiderT {
	@Autowired
	TengxunMobilePageSpider mobilePageSpider;
	
	@Test
    public void testHomeBanner()  {
        Job job = new Job();
        job.setUrl("https://m.v.qq.com/index.html#MOBILE_HOME_BANNER");
        
        // request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(mobilePageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<BannerRecommendation> banners = resultItems.get(BannerRecommendation.class.getSimpleName());
        if(null!=banners) {
        	banners.forEach(System.out::println);
        }
    }
	
	@Test
    public void testChannelBanner()  {
        Job job = new Job();
        job.setUrl("https://m.v.qq.com/x/m/channel/figure/tv#MOBILE_CHANNEL_BANNER");
        
        // request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(mobilePageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<BannerRecommendation> banners = resultItems.get(BannerRecommendation.class.getSimpleName());
        if(null!=banners) {
        	banners.forEach(System.out::println);
        }
    }
}
