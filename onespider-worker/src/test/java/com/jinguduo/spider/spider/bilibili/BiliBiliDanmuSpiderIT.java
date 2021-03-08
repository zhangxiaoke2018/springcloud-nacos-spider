package com.jinguduo.spider.spider.bilibili;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by jack on 2017/7/10.
 */

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class BiliBiliDanmuSpiderIT {

    @Autowired
    BiliBiliDanmuSpider biliDanmuSpider;
    
    @Test
    public void testContext() {
        Assert.notNull(biliDanmuSpider);
    }

    @Test
    public void commentTest(){
        Job job = new Job("https://comment.bilibili.com/61644239.xml");
        job.setPlatformId(1);
        job.setShowId(1);
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliDanmuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageText> barrageTexts = resultItems.get(BarrageText.class.getSimpleName());
        Assert.notNull(barrageTexts);
    }
}
