package com.jinguduo.spider.spider.movie56;

import java.io.IOException;

import java.util.List;

import org.apache.log4j.Logger;
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

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/15 下午2:33
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class Movie56WangSpiderIT {

    private static final Logger LOGGER = Logger.getLogger(Movie56WangSpiderIT.class);

    @Autowired
    private Movie56WangSpider movie56WangSpider;

    @Test
    public void testContext() {
        Assert.notNull(movie56WangSpider);
    }

    @Test
    public void processNetMovie()  {

        Job job = new Job("http://www.56.com/u49/v_MTM3ODg4Mzgy.html");
//        job.setCode("MTM3ODg4Mzgy");
        job.setCode("80681214");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(movie56WangSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobList = (List<Job>) resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(jobList.size() >= 0);
    }
    @Test
    public void processNetMovie2()  {

        Job job = new Job("http://www.56.com/u59/v_MTM1ODcyNDAw.html");
        job.setCode("MTM1ODcyNDAw");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(movie56WangSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get(Job.class.getSimpleName()));
    }
    @Test
    public void testa() throws IOException {


    }
}
