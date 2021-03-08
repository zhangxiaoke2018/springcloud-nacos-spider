package com.jinguduo.spider.spider.weibo;

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
import com.jinguduo.spider.data.text.WeiboHotSearchText;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class WeiboMobileSpiderTest {
	@Autowired
	private WeiboMobileSpider WeiboMobileSpider;
	
	private final String hotSearchPath = "http://api.weibo.cn/2/guest/page?c=android&page=1&count=100&containerid=106003type%3D25%26filter_type%3Drealtimehot";
	
	@Test
	public void testFetchHotwords() {
		Job job = new Job(hotSearchPath);
		// request
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(WeiboMobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

	   List<WeiboHotSearchText> result = resultItems.get(WeiboHotSearchText.class.getSimpleName());
	   for(WeiboHotSearchText item:result) {
		   System.out.println(item);
	   }
	}
}
