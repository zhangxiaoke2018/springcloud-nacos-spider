package com.jinguduo.spider.spider.manmanmanhua;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.webmagic.ResultItems;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by lc on 2019/4/18
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ComicMmmhOpenSpiderIT {

    @Autowired
    ComicMmmhOpenSpider spider;

    private String url ="https://open.manmanapp.com/?createTask=1";

    private String url2 ="https://open.manmanapp.com/guduo/guduomedia?sign=7f0e4028bd018f70e3beb469486c9487&time=1592192626";

    @Test
    public void testDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(url2);
        job.setCode("code");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
    }
}