package com.jinguduo.spider.spider.youku;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/18 下午2:48
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuDetailSpiderIT {

    @Autowired
    private YoukuDetailSpider youkuDetailSpider;

    @Test
    public void testProcessMain()  {
        Job job = new Job("https://list.youku.com/show/id_z2b70efbfbd0fefbfbd39.html" );

        job.setCode("z2b70efbfbd0fefbfbd39");

        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuDetailSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testProcessMain2()  {
        Job job = new Job("https://v.youku.com/v_show/id_XNDc3MzM1MDU4MA==.html?s=dbfeb7809a8244278e8e" );
        job.setCode("XNDc3MzM1MDU4MA==");

        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuDetailSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testProcessMain3()  {
        Job job = new Job("https://v.youku.com/v_show/id_XNDU2OTc4ODk3Ng==.html?s=2b70efbfbd0fefbfbd39" );
        job.setCode("XNDU2OTc4ODk3Ng==");


        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuDetailSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testTheaterMovie()  {
        Job job = new Job("http://v.youku.com/v_show/id_XMzUzMTc1NDAxNg==.html" ); //院线电影
        job.setCode("XMzUzMTc1NDAxNg==");

        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuDetailSpider)
            .addPipeline(testPipeline)
            .addRequest(delayRequest)
            .setDownloader(new DownloaderManager())
            .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad!");
    }
    
    @Test
    public void testTheaterMovie2()  {
        Job job = new Job("http://v.youku.com/v_show/id_XMjg3MTY0Mjc5Ng==.html" ); //院线电影
        job.setCode("XMjg3MTY0Mjc5Ng==");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuDetailSpider)
            .addPipeline(testPipeline)
            .addRequest(delayRequest)
            .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad!");
    }
}
