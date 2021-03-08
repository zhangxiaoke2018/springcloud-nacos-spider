package com.jinguduo.spider.spider.sohu;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;
import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;
import org.springframework.util.Base64Utils;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SohuSpiderIT {

    @Autowired
    private SohuSpider sohuSpider;

    @Test
    public void testKey()  {
        Job job = new Job("https://tv.sohu.com/item/MTIyNzc0Ng==.html");
        job.setCode("9559725");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new SohuSpiderUrlRewriter()))
        	.addPipeline(testPipeline)
        	.addRequest(delayRequest)
        	.run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testPlayCount()  {
        Job playCountJob = new Job("http://tv.sohu.com/item/VideoServlet?source=sohu&id=9457553&year=2018&month=0&page=0");
        playCountJob.setCode("9457553");
        DelayRequest playCountDelayRequest = new DelayRequest(playCountJob);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(playCountDelayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(showLogs, "Bad");
    }
    
    @Test
    public void testNetDrama()  {
        Job job = new Job("https://tv.sohu.com/s2017/dsjdwxlra/");
        job.setCode("9402322");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testProcessNetMovie(){
        Job netMovieJob = new Job("http://tv.sohu.com/item/MTIwNTQ3Nw==.html");
        netMovieJob.setPlatformId(1);
        netMovieJob.setShowId(1);
        netMovieJob.setFrequency(100);
        netMovieJob.setCode("0");
        DelayRequest netMovieDelayRequest = new DelayRequest(netMovieJob);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(netMovieDelayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testProcessNetMovie2(){
        Job netMovieJob = new Job("http://tv.sohu.com/item/MTIwOTUzNw==.html");
        netMovieJob.setCode("09275734");
        DelayRequest netMovieDelayRequest = new DelayRequest(netMovieJob);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider)
        		.addPipeline(testPipeline)
        		.addRequest(netMovieDelayRequest)
        		.run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testZongyiProcess()  {
        Job playCountJob = new Job("http://tv.sohu.com/s2016/ttws/");
        playCountJob.setCode("9165808");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testZongyi2()  {
        Job playCountJob = new Job("https://tv.sohu.com/s2018/zysybwnhhj/");
        playCountJob.setCode("9457553");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testAnime()  {
        Job playCountJob = new Job("http://tv.sohu.com/item/MTIwMzEyOA==.html");
        playCountJob.setCode("9165808");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue("http://count.vrs.sohu.com/count/queryext.action?plids=9220898&callback=playCountVrs".equals(jobs.get(0).getUrl()), "Bad");
    }
    
    @Test
    public void testMovie()  {
        Job playCountJob = new Job("http://tv.sohu.com/item/MTIwODg2Ng==.html");
        playCountJob.setCode("9252314");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
    }

    public static void main(String[] args){
        String a = "1227140" ;
        String s = Base64Utils.encodeToString(a.getBytes());
        System.out.println(s);
    }


}
