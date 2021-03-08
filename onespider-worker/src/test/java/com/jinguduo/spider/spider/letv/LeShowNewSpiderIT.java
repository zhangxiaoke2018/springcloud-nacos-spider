package com.jinguduo.spider.spider.letv;


import java.util.List;

import lombok.extern.slf4j.Slf4j;
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
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
public class LeShowNewSpiderIT {

    @Autowired
    private LeShowNewSpider leShowNewSpider;

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        //电视 http://d.api.m.le.com/card/dynamic?id=10014722&cid=2&vid=28834428&platform=pc&type=episode
        //http://d.api.m.le.com/detail/getPeriod?pid=10036400&platform=pc
        job.setUrl("http://d.api.m.le.com/card/dynamic?id=10026574&cid=2&vid=30876991&platform=pc&type=episode");
        job.setFrequency(100);
        job.setMethod("GET");
        job.setCode("10033258");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(leShowNewSpider);
    }

    @Test
    public void testRun() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(leShowNewSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "resultItems is null");

        List<Job> jobs = (List<Job>)resultItems.get(Job.class.getSimpleName());
        List<Show> shows = (List<Show>)resultItems.get(Show.class.getSimpleName());

        Assert.notEmpty(jobs, "jobs is null");
        Assert.notEmpty(shows, "shos is null");

        shows.stream().forEach(show -> {
            final boolean[] commentOneToOne = {false};
            final boolean[] barrageOneToOne = {false};
            jobs.stream().forEach(job -> {
                if (show.getCode().equals(job.getCode()) && job.getUrl().contains("danmu")) {
                    barrageOneToOne[0] = true;
                }
                if (show.getCode().equals(job.getCode()) && job.getUrl().contains("vcm")) {
                    commentOneToOne[0] = true;
                }
            });
            Assert.isTrue(commentOneToOne[0] && barrageOneToOne[0], "Shows of job is not full!");
            Assert.isTrue(commentOneToOne[0], "Shows of comment job is null! code :" + show.getCode());
            Assert.isTrue(barrageOneToOne[0], "Shows of barrage job is null! code :" + show.getCode());
        });
    }

}
