package com.jinguduo.spider.spider.jianshu;

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

/**
 * Created by lc on 2020/1/8
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SearchTest {

    @Autowired
    JianshuSearchSpider spider;

    private String testUrl = "https://www.jianshu.com/search/do?q=凌晨&type=note&page=1&order_by=default";


    @Test
    public void testInvolveCount() {

        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(testUrl);
        job.setCode("code");
        job.setMethod("POST");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
    }


}
