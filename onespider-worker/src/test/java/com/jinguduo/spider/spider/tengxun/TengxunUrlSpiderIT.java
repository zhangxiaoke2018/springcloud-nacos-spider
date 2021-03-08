package com.jinguduo.spider.spider.tengxun;


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
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunUrlSpiderIT {

    @Autowired
    private TengxunUrlSpider tengxunUrlSpider;
    
    @Test
    public void testDramaBanner()  {
        Job playCountJob = new Job("https://v.qq.com/tv/");
        playCountJob.setCode("1077824427");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
        
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        Assert.notEmpty(shows, "Bad");
    }
    
    @Test
    public void testRun()  {
    	Job job = new Job();
    	job.setUrl("https://v.qq.com/x/list/child?iarea=2&offset=0");
    	job.setCode("61ztu76ocstn3af");
    	// request
    	DelayRequest delayRequest = new DelayRequest(job);
    	
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testNetMovie()  {
    	Job job = new Job("https://v.qq.com/x/cover/bnc0cvuskzxnrcl/j0021k88nw1.html");
    	job.setCode("3bz8a5vx2nra8p5");
    	// request
    	DelayRequest delayRequest = new DelayRequest(job);
    	
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobList, "Bad");
    }

    @Test
    public void testDetailPage()  {
        Job job = new Job();
        job.setUrl("https://v.qq.com/detail/d/dxd1v76tmu0wjuj.html");
        job.setCode("dxd1v76tmu0wjuj");
        // request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testNetVariety()  {
        Job playCountJob = new Job("https://v.qq.com/detail/8/86621.html");
        //playCountJob.setCode("79752");
        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }


    @Test
    public void testVarietyEpi()  {
        Job playCountJob = new Job("https://v.qq.com/x/cover/0fe566w3k2kgr84.html");
        playCountJob.setCode("0fe566w3k2kgr84");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testProcessVarietyTPC()  {
        Job playCountJob = new Job("http://v.qq.com/variety/column/column_21953.html");
        playCountJob.setCode("21953");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(showLogs, "Bad");
    }

    @Test
    public void testAnime()  {
        Job playCountJob = new Job("http://v.qq.com/detail/n/nilk5fd4bkqdk3a.html");
        playCountJob.setCode("nilk5fd4bkqdk3a");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        ShowLog showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs, "Bad");
        
        Show show = resultItems.get(Show.class.getSimpleName());
        Assert.notNull(show, "Bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testHomeScan() throws Exception {
        Job j = new Job("https://v.qq.com");
        j.setCode("bbbbb");
        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get("Job");
        Assert.notEmpty(jobs,"home scan fail");
    }

    @Test
    public void testDetaill() {
        Job j = new Job("http://v.qq.com/detail/7/74398.html");
        j.setCode("74398");
        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get("Job");

        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testBanner() throws Exception {
        Job j = new Job("https://v.qq.com/tv/");
        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get("Job");

        Assert.notEmpty(jobs,"home scan fail");
    }
    @Test
    public void testHomeBanner()  {
        Job job = new Job();
        job.setUrl("https://v.qq.com#WEB_HOME_BANNER");
 
        // request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<BannerRecommendation> banners = resultItems.get(BannerRecommendation.class.getSimpleName());
        if(null!=banners) {
        	banners.forEach(System.out::println);
        }
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        if(null!=jobs) {
        	jobs.forEach(System.out::println);
        }
    }
    
    @Test
    public void testChannelBanner()  {
        Job job = new Job();
        job.setUrl("https://v.qq.com/channel/tv#WEB_CHANNEL_BANNER");
        
        // request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunUrlSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<BannerRecommendation> banners = resultItems.get(BannerRecommendation.class.getSimpleName());
        if(null!=banners) {
        	banners.forEach(System.out::println);
        }
    }
}
