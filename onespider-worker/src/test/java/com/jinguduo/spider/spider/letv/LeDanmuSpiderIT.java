package com.jinguduo.spider.spider.letv;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.ResultItems;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class LeDanmuSpiderIT {

    @Autowired
    private LeDanmuSpider leDanmuSpider;

    private final static String URL = "http://cdn.api.my.letv.com/danmu/list?vid=27247728&cid=2&start=0&amount=2000&getcount=1";

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");
        job.setCode("14bsaa5s4z4g8qo");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void getTextTest()  {

        Job mainJob = new Job("http://cdn.api.my.letv.com/danmu/list?vid=27247728&cid=2&start=0&amount=2000&getcount=1");
        mainJob.setPlatformId(1);
        mainJob.setShowId(1);
        mainJob.setFrequency(100);
        mainJob.setMethod("GET");
        mainJob.setCode("asdsadsadsadasdas");

        DelayRequest delayRequest = new DelayRequest(mainJob);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(leDanmuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageText> barrageText = resultItems.get(BarrageText.class.getSimpleName());
        List<Job> job = resultItems.get(Job.class.getSimpleName());

        Assert.isTrue(CollectionUtils.isNotEmpty(barrageText));
        Assert.notNull(job.get(0).getUrl());

    }
}
