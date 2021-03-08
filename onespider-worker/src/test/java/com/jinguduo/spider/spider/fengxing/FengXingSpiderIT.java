package com.jinguduo.spider.spider.fengxing;


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
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/14 下午4:20
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class FengXingSpiderIT {

    @Autowired
    private FengXingSpider fengXingSpider;

    private static final String URL = "http://www.fun.tv/vplay/g-312359/";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job(URL);
        job.setPlatformId(1);
        job.setFrequency(100);
        job.setCode("g-312359");

        delayRequest = new DelayRequest(job);
    }
    @Test
    public void testContext() {
        Assert.notNull(fengXingSpider);
    }
    
    @Test
    public void playerTest(){
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(fengXingSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(showLog);
        Assert.notNull(job);
    }
}
