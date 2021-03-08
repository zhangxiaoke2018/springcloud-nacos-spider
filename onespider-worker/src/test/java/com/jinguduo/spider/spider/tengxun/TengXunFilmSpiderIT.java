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

public class TengXunFilmSpiderIT {

    @Autowired
    private TengXunFilmSpider tengXunFilmSpider;

    final static String URL = "http://v.qq.com/x/cover/f9o85jx5rmbfl8d.html?vid=h001792simj";

    final static String CODE = "f9o85jx5rmbfl8d";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job(URL);
        job.setFrequency(100);
        job.setMethod("GET");
        job.setCode(CODE);

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(tengXunFilmSpider);
    }


    @Test
    public void testFilm() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengXunFilmSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems);

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs);

    }
}
