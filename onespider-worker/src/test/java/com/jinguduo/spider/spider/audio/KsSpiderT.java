package com.jinguduo.spider.spider.audio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;
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
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;
import com.jinguduo.spider.webmagic.utils.HttpConstant.Method;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class KsSpiderT {
	
	@Autowired
	KaishuMobileSpider spider;
	
	@Test
	public void testToken() {
		String errorUrl = "http://api.kaishustory.com/top/story/hot?page_no=1&page_size=10";


		
		Job job = new Job();
		job.setMethod(Method.GET);
		job.setUrl(errorUrl);
		job.setCode(Md5Util.getMd5(errorUrl));
		job.setPlatformId(Platform.KS_STORY.getCode());
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(job);
		List<SpiderListener> sl = new ArrayList<SpiderListener>();
		sl.add(new KaishuTokenSpiderListener());
		
		
		
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).addSpiderListeners(sl).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		
		jobs.forEach(System.out::println);
		

		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(new DelayRequest(jobs.get(0))).addSpiderListeners(sl).run();
		
		resultItems = testPipeline.getResultItems();
		
		List<Job> jobs2 = resultItems.get(Job.class.getSimpleName());
		
		jobs2.forEach(System.out::println);
	}
	
	@Test
	public void testEntrance() {
		String url = "http://api.kaishustory.com/top/story/hot?page_no=1&page_size=10&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6ZmFsc2UsImlzcyI6ImthaXNodXN0b3J5IiwiZXhwIjoyNTYwNjcwMzQyLCJ1ZCI6MH0.YHepSgPDoPD9M5rTyEYc4rtABTIH8Y7YFS1fIBH1mhg";
		Job job = new Job();
		job.setUrl(url);
		job.setCode(Md5Util.getMd5(url));
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(job);
		List<SpiderListener> sl = new ArrayList<SpiderListener>();
		sl.add(new KaishuTokenSpiderListener());
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).addSpiderListeners(sl).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());

		jobs.forEach(System.out::println);
	}
	
	@Test
	public void testList() {
		String url = "http://api.kaishustory.com/top/story/hot?page_no=2&page_size=10&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6ZmFsc2UsImlzcyI6ImthaXNodXN0b3J5IiwiZXhwIjoyNTYwNjcwMzQyLCJ1ZCI6MH0.YHepSgPDoPD9M5rTyEYc4rtABTIH8Y7YFS1fIBH1mhg";
		Job job = new Job();
		job.setUrl(url);
		job.setCode(Md5Util.getMd5(url));
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(job);
		List<SpiderListener> sl = new ArrayList<SpiderListener>();
		sl.add(new KaishuTokenSpiderListener());
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).addSpiderListeners(sl).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());

		jobs.forEach(System.out::println);
	}
	
	@Test
	public void testListVIP() {
		String url = "http://api.kaishustory.com/top/story/product/vip?page_no=2&page_size=10&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6ZmFsc2UsImlzcyI6ImthaXNodXN0b3J5IiwiZXhwIjoyNTYwNjcwMzQyLCJ1ZCI6MH0.YHepSgPDoPD9M5rTyEYc4rtABTIH8Y7YFS1fIBH1mhg";
		Job job = new Job();
		job.setUrl(url);
		job.setCode(Md5Util.getMd5(url));
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(job);
		List<SpiderListener> sl = new ArrayList<SpiderListener>();
		sl.add(new KaishuTokenSpiderListener());
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).addSpiderListeners(sl).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Job> jobs = resultItems.get(Job.class.getSimpleName());

		jobs.forEach(System.out::println);
	}
	
	
	
	@Test
	public void testDetail() {
		String url = "http://api.kaishustory.com/product/detail/storylist?page_size=200&productid=2698&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6ZmFsc2UsImlzcyI6ImthaXNodXN0b3J5IiwiZXhwIjoyNTYwNjcwMzQyLCJ1ZCI6MH0.YHepSgPDoPD9M5rTyEYc4rtABTIH8Y7YFS1fIBH1mhg";
		Job job = new Job();
		job.setUrl(url);
		job.setCode(Md5Util.getMd5(url));
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(job);
		List<SpiderListener> sl = new ArrayList<SpiderListener>();
		sl.add(new KaishuTokenSpiderListener());
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).addSpiderListeners(sl).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Audio> audio = resultItems.get(Audio.class.getSimpleName());

		audio.forEach(System.out::println);
		
		List<AudioVolumeLog> vlog = resultItems.get(AudioVolumeLog.class.getSimpleName());

		vlog.forEach(System.out::println);
		
		List<AudioPlayCountLog> pcLog = resultItems.get(AudioPlayCountLog.class.getSimpleName());

		pcLog.forEach(System.out::println);
	}
	
	@Test
	public void testDetail2() {
		String url = "http://api.kaishustory.com/product/detail/storylist?page_size=200&productid=2709&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6ZmFsc2UsImlzcyI6ImthaXNodXN0b3J5IiwiZXhwIjoyNTYwNjcwMzQyLCJ1ZCI6MH0.YHepSgPDoPD9M5rTyEYc4rtABTIH8Y7YFS1fIBH1mhg";
		Job job = new Job();
		job.setUrl(url);
		job.setCode(Md5Util.getMd5(url));
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(job);
		List<SpiderListener> sl = new ArrayList<SpiderListener>();
		sl.add(new KaishuTokenSpiderListener());
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).addSpiderListeners(sl).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Audio> audio = resultItems.get(Audio.class.getSimpleName());

		audio.forEach(System.out::println);
		
		List<AudioVolumeLog> vlog = resultItems.get(AudioVolumeLog.class.getSimpleName());

		vlog.forEach(System.out::println);
		
		List<AudioPlayCountLog> pcLog = resultItems.get(AudioPlayCountLog.class.getSimpleName());

		pcLog.forEach(System.out::println);
	}
	
	@Test
	public void testStoryDetail() {
		String url = "http://api.kaishustory.com/storyservice/story/findbyid?id=102733&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6ZmFsc2UsImlzcyI6ImthaXNodXN0b3J5IiwiZXhwIjoyNTYwNjcwMzQyLCJ1ZCI6MH0.YHepSgPDoPD9M5rTyEYc4rtABTIH8Y7YFS1fIBH1mhg";
		Job job = new Job();
		job.setUrl(url);
		job.setCode(Md5Util.getMd5(url));
		
		TestPipeline testPipeline = new TestPipeline();
		DelayRequest delayRequest = new DelayRequest(job);
		List<SpiderListener> sl = new ArrayList<SpiderListener>();
		sl.add(new KaishuTokenSpiderListener());
		SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).addSpiderListeners(sl).run();
		ResultItems resultItems = testPipeline.getResultItems();
		
		List<Job> j = resultItems.get(Job.class.getSimpleName());

		j.forEach(System.out::println);
	}
}
