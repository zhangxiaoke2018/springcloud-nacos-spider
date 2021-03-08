package com.jinguduo.spider.spider.weibo;


import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.WeiboTagLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class WeiboTagSpiderIT {

    @Autowired
    private WeiboTagSpider weiboTagSpider;
    
    private final static String WEB_TOPIC_URL = "https://huati.weibo.com/k/肖战";

    @Test
    public void testWeiboTopic() {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(WEB_TOPIC_URL);
        job.setCode("6941");
        DelayRequest delayRequest = new DelayRequest(job);
        
        SpiderEngine.create(weiboTagSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        
        ResultItems resultItems = testPipeline.getResultItems();
//        Assert.notNull(resultItems);

        List<WeiboTagLog> weiboTagLog = resultItems.get(WeiboTagLog.class.getSimpleName());
        Assert.notNull(weiboTagLog);
        Assert.isTrue(weiboTagLog.get(0).getFeedCount() > 0);
        Assert.isTrue(weiboTagLog.get(0).getReadCount() > 0);
        Assert.isTrue(weiboTagLog.get(0).getFollowCount() > 0);
        Assert.isTrue(weiboTagLog.get(0).getKeyword().equals("王俊凯"));
    }
}
