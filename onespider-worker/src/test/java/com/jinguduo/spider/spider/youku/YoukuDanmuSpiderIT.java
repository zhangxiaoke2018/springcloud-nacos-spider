package com.jinguduo.spider.spider.youku;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * Created by Baohao on 2016/12/16.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuDanmuSpiderIT {

    @Autowired
    private YoukuDanmuSpider youkuDanmuSpider;

    final static String URL = "http://service.danmu.youku.com/list?iid=471618915&ct=1001&cid=97&type=1&aid=307573&lid=0&mcount=1&uid=0&ouid=1066650389&mat=2";

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
    public void testExMovie()  {
        Job job = new Job("http://service.danmu.youku.com/pool/info?iid=452362985&ct=1001");
        job.setPlatformId(1);
        job.setCode("1");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuDanmuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<BarrageLog> barrageLogs = resultItems.get(BarrageLog.class.getSimpleName());
        Assert.notNull(barrageLogs);
        System.out.println(barrageLogs);

    }

    @Test
    public void getDanmuTest() {
        Job mainJob = new Job("http://service.danmu.youku.com/list?iid=471618915&ct=1001&cid=97&type=1&aid=307573&lid=0&mcount=1&uid=0&ouid=1066650389&mat=2");
        mainJob.setPlatformId(1);
        mainJob.setShowId(1);
        mainJob.setFrequency(100);
        mainJob.setMethod("GET");
        mainJob.setCode("asdsadsadsadasdas");

        DelayRequest delayRequest1 = new DelayRequest(mainJob);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuDanmuSpider).addPipeline(testPipeline).addRequest(delayRequest1).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageText> barrageText = resultItems.get(BarrageText.class.getSimpleName());
        List<Job> job = resultItems.get(Job.class.getSimpleName());

        Assert.isTrue(CollectionUtils.isNotEmpty(barrageText));
        Assert.notNull(job.get(0).getUrl());
    }
}
