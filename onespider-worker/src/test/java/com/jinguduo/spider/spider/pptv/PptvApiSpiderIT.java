package com.jinguduo.spider.spider.pptv;


import java.util.List;

import org.apache.log4j.Logger;
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
public class PptvApiSpiderIT {

    private static final Logger log = Logger.getLogger(PptvApiSpiderIT.class);

    @Autowired
    private PptvApiSpider pptvApiSpider;

    @Test
    public void testContext() {
        Assert.notNull(pptvApiSpider);
    }

    @Test
    public void processShowList() throws Exception {
        // 网剧
        // http://epg.api.pptv.com/detail.api?cb=recDetailData&auth=4EYysBhib7iayPDXU&vid=9038048&subId=20365273
        // 电影合集
        // http://epg.api.pptv.com/detail.api?cb=recDetailData&auth=tzIZlv5k1BJ181s&vid=983567&subId=25445303
        // 单集电影
        // http://epg.api.pptv.com/detail.api?cb=recDetailData&auth=tzIZlv5k1BJ181s&vid=25492511&subId=25492511
        String PLAYCOUNT_URL = "http://epg.api.pptv.com/detail.api?cb=recDetailData&platform=android3&auth=iavuQD3fdTYvubNQ&vid=9049289&subId=30816138";
        Job job = new Job(PLAYCOUNT_URL);
        job.setCode("iavuQD3fdTYvubNQ");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(pptvApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        System.out.println(ShowLog.class.getSimpleName());
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.isTrue(showLogs != null);
    }

}
