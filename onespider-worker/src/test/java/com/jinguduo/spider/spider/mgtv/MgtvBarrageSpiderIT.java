package com.jinguduo.spider.spider.mgtv;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.spider.mgtv.MgtvBarrageSpider;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.pipeline.ConsolePipeline;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class MgtvBarrageSpiderIT {

    @Autowired
    private MgtvBarrageSpider spider;

    private DelayRequest delayRequest;

    private final static String URL = "http://galaxy.person.mgtv.com/rdbarrage?&time=0&vid=4068250&cid=316381";

    private final static String CODE = "4068250";

    @Before
    public void setup()  {
        Job job = new Job(URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode(CODE);

        //simulate request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testTextProcess() {
        TestPipeline testPipeline = new TestPipeline();

        SpiderEngine.create(spider)
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(delayRequest)
                .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<BarrageText> barrageTexts = resultItems.get(BarrageText.class.getSimpleName());

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());

        Assert.isTrue(
                CollectionUtils.isNotEmpty(barrageTexts)
                        && StringUtils.hasText(barrageTexts.get(0).getContent())
                        && CODE.equals(barrageTexts.get(0).getCode()),
                "textProcess text fail by url : " + URL
        );

        Assert.isTrue(
                CollectionUtils.isNotEmpty(jobs)
                        && CODE.equals(jobs.get(0).getCode()
                ),
                "textProcess next job fail by url : " + URL
        );
    }
}
