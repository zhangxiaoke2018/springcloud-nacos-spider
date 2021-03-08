package com.jinguduo.spider.spider.tengxun;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

@CommonsLog
public class TengxunTotalPlayCountSpiderIT {

	@Autowired
	private TengxunTotalPlayCountSpider tengxunTotalPlayCountSpider;
	

	@Test
	public void testContext() {
		Assert.notNull(tengxunTotalPlayCountSpider);
	}

	@Test
	public void testNetMovie()  {
		Job job = new Job();
		job.setPlatformId(1);
		job.setShowId(1);
		job.setCode("o0ytzgvq6o08e9o");
		job.setUrl("http://data.video.qq.com/fcgi-bin/data?tid=70&&appid=10001007&appkey=e075742beb866145&callback=jQuery19109213305850191142_1468217242170&low_login=1&idlist=gesxqiorblxn117&otype=json&_=1468217242171");
		job.setFrequency(100);

		// request
		DelayRequest delayRequest = new DelayRequest(job);
		
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(tengxunTotalPlayCountSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		
		List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
		log.debug(showLog);
		Assert.notNull(showLog);
		Assert.notNull(showLog.get(0).getCode());
		Assert.isTrue(showLog.get(0).getPlayCount() > 0);

	}
}
