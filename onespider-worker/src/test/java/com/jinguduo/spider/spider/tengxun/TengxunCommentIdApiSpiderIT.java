package com.jinguduo.spider.spider.tengxun;


import java.util.List;

import org.junit.Before;
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
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunCommentIdApiSpiderIT {

    @Autowired
    private TengxunCommentIdApiSpider tengxunCommentIdApiSpider;

    private final static String URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=3&vid=e0030fall9v";

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");
        job.setCode("h0838go3473");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(tengxunCommentIdApiSpider);
    }


    @Test
    public void testRun() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxunCommentIdApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);
    }
}
