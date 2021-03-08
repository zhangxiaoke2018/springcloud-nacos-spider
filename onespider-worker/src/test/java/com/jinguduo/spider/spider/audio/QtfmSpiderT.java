package com.jinguduo.spider.spider.audio;

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
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class QtfmSpiderT {
	@Autowired
	private QtfmMobileSpider spider;
	
	@Test
	public void testEntrance() {
		String entrance = "https://webapi.qingting.fm/api/mobile/search/layer";
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(new Job(entrance));
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		if(null!=jobs)
			jobs.forEach(System.out::println);
	}
	
	@Test
	public void testRank1() {
		String entrance = 
				"https://webapi.qingting.fm/api/mobile/categories/521/attr/0/?page=1";
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(new Job(entrance));
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		if(null!=jobs)
			jobs.forEach(System.out::println);
	}
	
	@Test
	public void testRank2() {
		String entrance = 
				"https://webapi.qingting.fm/api/mobile/categories/521/attr/playcount/?page=2";
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(new Job(entrance));
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		if(null!=jobs)
			jobs.forEach(System.out::println);
	}
	
	@Test
	public void testDetail() {
		String entrance = 
				"https://webapi.qingting.fm/api/mobile/channels/296414";
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(new Job(entrance));
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
		ResultItems resultItems = testPipeline.getResultItems();

		List<Audio> audios = resultItems.get(Audio.class.getSimpleName());
		if(null!=audios)
			audios.forEach(System.out::println);
		
		List<AudioPlayCountLog> pc = resultItems.get(AudioPlayCountLog.class.getSimpleName());
		if(null!=pc)
			pc.forEach(System.out::println);
		
		List<AudioVolumeLog> vl = resultItems.get(AudioVolumeLog.class.getSimpleName());
		if(null!=vl)
			vl.forEach(System.out::println);
	}
	
}
