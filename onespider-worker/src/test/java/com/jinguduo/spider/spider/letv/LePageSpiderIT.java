package com.jinguduo.spider.spider.letv;


import java.util.List;

import org.junit.Before;
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
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class LePageSpiderIT {

    @Autowired
    private LePageSpider leTvPageSpider;

    @Before
    public void setup()  {
    }

    @Test
    public void testContext() {
        Assert.notNull(leTvPageSpider);
    }

    @Test
    public void testRun()  {
        Job job = new Job("http://www.le.com/tv/10026574.html");
        job.setFrequency(100);
        job.setCode("10033258");
        job.setMethod("GET");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(leTvPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems);

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(jobs.size() == 2);
    }
}
