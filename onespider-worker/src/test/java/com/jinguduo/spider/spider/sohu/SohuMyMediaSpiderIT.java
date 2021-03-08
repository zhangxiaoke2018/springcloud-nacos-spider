package com.jinguduo.spider.spider.sohu;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SohuMyMediaSpiderIT {

    @Autowired
    private SohuMyMediaSpider sohuSpider;

    @Before
    public void setup()  {

    }

    @Test
    public void testContext() {
        Assert.notNull(sohuSpider);
    }

    @Test
    public void testListPage()  {
        Job job = new Job("http://my.tv.sohu.com/pl/9360574/index.shtml");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("9360574");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Show> show = resultItems.get(Show.class.getSimpleName());
        Assert.isTrue(show.size()>=0);
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(jobs.size()>=0);
    }


}
