package com.jinguduo.spider.spider.letv;


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
import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class LePlayCountSpiderIT {

    @Autowired
    private LePlayCountSpider leTvPlayCountApiSpider;

    final static String EPI_URL = "http://v.stat.letv.com/vplay/queryMmsTotalPCount?vid=25454271";

    final static String TOTAL_URL = "http://v.stat.letv.com/vplay/queryMmsTotalPCount?pid=85010";

    DelayRequest delayRequest;
    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(TOTAL_URL);
        job.setFrequency(100);
        job.setMethod("GET");
        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(leTvPlayCountApiSpider);
    }

    @Test
    public void testRun() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(leTvPlayCountApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()));
        Assert.notNull(resultItems.get(CommentLog.class.getSimpleName()));
        Assert.notNull(resultItems.get(BarrageLog.class.getSimpleName()));
    }
}
