package com.jinguduo.spider.spider.youku;

import java.util.ArrayList;

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
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuSearchSpiderIT {

    @Autowired
    private YoukuSearchSpider youkuSearchSpider;

    @Test
    public void testContext() {
        Assert.notNull(youkuSearchSpider);
    }

    @Test
    public void testSearchPage()  {
        Job job = new Job("http://www.soku.com/search_video/q_刺客伍六七" );
        job.setCode("zd4a1c61a5c114a6e89e9");

        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSearchSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        ArrayList<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs);
    }
}
