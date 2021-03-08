package com.jinguduo.spider.spider.piaofang;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.BoxOfficeLogs;
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
public class ChinaBoxOfficeSpiderIT {

    @Autowired
    private ChinaBoxOfficeSpider chinaBoxOfficeSpider;

    @Test
    public void testBoxOffice(){
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://www.cbooo.cn/m/657862");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(chinaBoxOfficeSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BoxOfficeLogs> boxOffices=resultItems.get(BoxOfficeLogs.class.getSimpleName());
        Assert.isTrue(boxOffices.size() > 0);
    }
}
