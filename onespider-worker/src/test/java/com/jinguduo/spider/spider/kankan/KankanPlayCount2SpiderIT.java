package com.jinguduo.spider.spider.kankan;

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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class KankanPlayCount2SpiderIT {

    @Autowired
    private KanKanPlayCountSpider kanKanPlayCountSpider;
    
    private static final String code = "68810";

    private static final String URL = "http://movie.kankan.com/movie/";

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job(URL+code);
        job.setPlatformId(1);
        job.setFrequency(100);
        job.setCode(code);

        delayRequest = new DelayRequest(job);
    }
    @Test
    public void testContext() {
        Assert.notNull(kanKanPlayCountSpider);
    }
    
    @Test
    public void process(){
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(kanKanPlayCountSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLog);
    }
    
    @Test
    public void processList(){
        Job job = new Job("http://movie.kankan.com/down_js/68/68810.js");
        job.setPlatformId(1);
        job.setFrequency(100);
        job.setCode(code);
        delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(kanKanPlayCountSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems);
    }
}
