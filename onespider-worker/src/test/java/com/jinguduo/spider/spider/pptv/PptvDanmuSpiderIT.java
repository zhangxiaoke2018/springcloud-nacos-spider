package com.jinguduo.spider.spider.pptv;

import com.jinguduo.spider.WorkerMainApplication;
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

/**
 * Created by csonezp on 2016/11/24.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class PptvDanmuSpiderIT {
    @Autowired
    private PptvDanmuSpider spider;

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        // Job
        Job mainJob = new Job("http://apicdn.danmu.pptv.com/danmu/v2/pplive/ref/vod_25417273/danmu?pos=0");
        mainJob.setPlatformId(1);
        mainJob.setShowId(1);
        mainJob.setFrequency(100);
        mainJob.setMethod("GET");
        mainJob.setCode("asdsadsadsadasdas");

        // Request
        delayRequest = new DelayRequest(mainJob);
    }
    @Test
    public void testContext() {
        Assert.notNull(spider);
    }

    /***
     * 弹幕文本测试
     * @
     */
    @Test
    public void contentTest ()  {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageText> barrageText = resultItems.get(BarrageText.class.getSimpleName());
        List<Job> job = resultItems.get(Job.class.getSimpleName());

        Assert.isTrue(CollectionUtils.isNotEmpty(barrageText));
        Assert.notNull(job.get(0).getUrl());
    }
}
