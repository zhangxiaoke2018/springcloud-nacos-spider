package com.jinguduo.spider.spider.mgtv;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

import lombok.extern.apachecommons.CommonsLog;
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
 * Created by gsw on 2017/2/3.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

@CommonsLog
public class MgtvAnimePlayCountSpiderIT {

    @Autowired
    private MgtvAnimePlayCountSpider mgtvAnimePlayCountSpider;

    final static String URL = "http://vc.mgtv.com/v2/dynamicinfo?cid=293994";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job(URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("291415");
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testRun() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(mgtvAnimePlayCountSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(showLogs);
        log.debug(showLogs);

    }
}
