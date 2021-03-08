package com.jinguduo.spider.spider.tengxun;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.BarrageLog;
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
public class TengxunDanmuSpiderIT {
    @Autowired
    private TengxunDanmuSpider spider;

    final static String URL ="https://mfm.video.qq.com/danmu?otype=json&target_id=4015285084&session_key=0%2C0%2C0&_=1479627541687";

    DelayRequest delayRequest;

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
    public void testContext() {
        Assert.notNull(spider);
    }

    @Test
    public void testRun() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageLog> barrageLog = resultItems.get(BarrageLog.class.getSimpleName());
        Assert.notNull(barrageLog);
    }

    /***
     * 弹幕文本测试
     * @
     */
    @Test
    public void contentTest ()  {
        // Job
        Job mainJob = new Job("https://mfm.video.qq.com/danmu?otype=json&timestamp=15&target_id=4015285084&count=10000&second_count=10000&session_key=0%2C0%2C0&_=1563332062462");
        mainJob.setPlatformId(1);
        mainJob.setShowId(1);
        mainJob.setFrequency(100);
        mainJob.setMethod("GET");
        mainJob.setCode("2023472120");

        // Request
        DelayRequest delayRequest = new DelayRequest(mainJob);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageText> barrageTexts = resultItems.get(BarrageText.class.getSimpleName());
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());

        Assert.isTrue(CollectionUtils.isNotEmpty(barrageTexts));
        Assert.notNull(jobs.get(0).getUrl());
        
        // 以下检查必须要！影响统计
        BarrageText barrage = barrageTexts.get(0);
        Assert.notNull(barrage);
        Assert.notNull(barrage.getBarrageId());
    }
}
