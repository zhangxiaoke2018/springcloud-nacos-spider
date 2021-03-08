package com.jinguduo.spider.spider.bilibili;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.ResultItems;

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
public class BiliBiliApiSpiderIT {

    @Autowired
    BiliBiliApiSpider biliBiliApiSpider;

    @Test
    public void testReply(){
        Job job = new Job("https://api.bilibili.com/x/v2/reply?pn=1&type=1&oid=34438162&sort=0");
        job.setPlatformId(24);
        job.setShowId(1);
        job.setCode("12345678");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliBiliApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notNull(commentTextList);
    }

    @Test
    public void testShowList(){
        Job job = new Job("https://api.bilibili.com/pgc/web/season/section?season_id=6159");

        job.setPlatformId(24);
        job.setShowId(1);
        job.setCode("12345678");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliBiliApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notNull(commentTextList);
    }
}
