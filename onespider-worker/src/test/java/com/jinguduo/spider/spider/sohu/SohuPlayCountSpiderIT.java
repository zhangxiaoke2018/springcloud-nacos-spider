package com.jinguduo.spider.spider.sohu;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ShowLog;
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
public class SohuPlayCountSpiderIT {

    @Autowired
    private SohuPlayCountSpider sohuSpider;

    @Test
    public void testPlayCount1()  {
        Job job = new Job("http://count.vrs.sohu.com/count/queryext.action?plids=9166337&callback=playCountVrs");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("9166337");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        Assert.isTrue(showLog.get(0).getPlayCount()>=0, "Bad!");
    }
    
    @Test
    public void testPlayCount2()  {
        Job job = new Job("http://count.vrs.sohu.com/count/queryext.action?vids=4689046&plids=9457553&callback=playCountVrs");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("4689046");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        Assert.isTrue(showLog.get(0).getPlayCount()>=0, "Bad!");
    }
}
