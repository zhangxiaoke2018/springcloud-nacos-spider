package com.jinguduo.spider.spider.u17;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class U17ComicSpiderIT {

    @Autowired
    private U17ComicSpider spider;

    @Test
    public void testRun(){
        Job j = new Job("https://www.u17.com/comic/134831.html");
        j.setCode("134831");

        DelayRequest delayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
    }
}
