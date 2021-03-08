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
public class PptvSearchIT {

    private static final Logger log = Logger.getLogger(PptvSearchIT.class);

    @Autowired
    private PPTVSearchSpider pptvSearchSpider;

    @Test
    public void search()  {
        Job job = new Job("http://search.pptv.com/s_video?kw=%E3%80%90%E4%B8%BB%E9%A2%98%E6%94%BE%E6%98%A0%E5%8E%85%E3%80%91%E4%BA%BA%E4%BA%BA%E9%83%BD%E7%88%B1%E2%80%9C%E5%A4%A7%E5%A5%B3%E4%B8%BB%E2%80%9D");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setCode("pVozsFYSC0msKpI");

        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();

        SpiderEngine.create(pptvSearchSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems);
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        log.debug(showLog);
        Assert.isTrue(showLog.get(0).getPlayCount().compareTo(0L)>=1);
    }
}
