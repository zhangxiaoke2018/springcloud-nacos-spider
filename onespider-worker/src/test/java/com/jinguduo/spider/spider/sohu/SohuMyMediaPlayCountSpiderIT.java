package com.jinguduo.spider.spider.sohu;

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
public class SohuMyMediaPlayCountSpiderIT {

    private final static String SINGLE_PLAYCOUNT = "http://vstat.my.tv.sohu.com/dostat.do?method=getVideoPlayCount&v=89479706&n=_stat";

    @Autowired
    private SohuMyMediaSinglePlayCountSpider sohuSpider;

    @Before
    public void setup()  {

    }

    @Test
    public void testContext() {
        Assert.notNull(sohuSpider);
    }

    @Test
    public void singleTest()  {
        Job job = new Job(SINGLE_PLAYCOUNT);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("89479706");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        Assert.isTrue(showLog.get(0).getPlayCount()>=0);
    }
}
