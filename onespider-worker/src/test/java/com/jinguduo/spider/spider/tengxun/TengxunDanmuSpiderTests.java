package com.jinguduo.spider.spider.tengxun;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.webmagic.ResultItems;

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

public class TengxunDanmuSpiderTests {

    @Autowired
    private TengxunDanmuSpider spider;

    final static String URL ="https://mfm.video.qq.com/danmu?otype=json&target_id=1625862006&session_key=0%2C0%2C0&_=1479627541687";

    DelayRequest delayRequest;


    final static String FILE = "/html/TengXunDanmuCount.Html";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(FILE);

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

}
