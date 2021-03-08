package com.jinguduo.spider.spider.tengxun;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;

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
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/1
 * Time:10:00
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ComicTengxunSpiderIT {

    @Autowired
    private TengxunComicSpider spider;

    private DelayRequest delayRequest;

    private final String url = "https://ac.qq.com/Comic/comicInfo/id/549462";

    private final String commentUrl = "http://ac.qq.com/Community/topicList?targetId=549462&page=1?_=1603900800000";



    private  final String url1 = "http://ac.qq.com/Comic/userComicInfo?comicId=629278?_=1603900800000";

    @Before
    public void setup()  {

        //loading job
        Job job = new Job(url1);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("code");

        //simulate request
        delayRequest = new DelayRequest(job);

    }

    @Test
    public void testInvolveCount() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);
    }
}
