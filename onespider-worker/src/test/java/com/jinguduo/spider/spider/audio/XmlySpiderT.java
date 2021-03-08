package com.jinguduo.spider.spider.audio;

import java.util.List;

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
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class XmlySpiderT {
	@Autowired
	private XmlyWebSpider spider;
	
	@Test
	public void testEntrance() {
		String entrance = "https://www.ximalaya.com";
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(new Job(entrance));
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		delayRequest = new DelayRequest(jobs.get(401));
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		List<AudioVolumeLog> volumes = resultItems.get(AudioVolumeLog.class.getSimpleName());
		Assert.notNull(volumes);
		System.out.println(volumes.get(0));
		
		List<AudioPlayCountLog> playCounts = resultItems.get(AudioPlayCountLog.class.getSimpleName());
		Assert.notNull(playCounts);
		System.out.println(playCounts.get(0));
		
		List<Job> detailJobs = resultItems.get(Job.class.getSimpleName());
		delayRequest = new DelayRequest(detailJobs.get(0));
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		resultItems = testPipeline.getResultItems();
		List<Audio> audios = resultItems.get(Audio.class.getSimpleName());
		Assert.notNull(audios);
		System.out.println(audios.get(0));
		
		
	}
}
