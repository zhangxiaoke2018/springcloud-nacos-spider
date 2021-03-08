package com.jinguduo.spider.spider.weibo;


import java.util.List;

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
import com.jinguduo.spider.data.table.WeiboOfficialLog;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class WeiboSpiderIT {

    @Autowired
    private WeiboOfficialApiSpider weiboOfficialApiSpider;

    private static String Weibo_URL = "http://weibo.com/xiaotan16?refer_flag=1001030101_&is_hot=1";

    @Before
    public void setup()  {

    }

    @Test
    public void testContext() {
        Assert.notNull(weiboOfficialApiSpider);
    }

    @Test
    public void testWeiboOfficialApi()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(Weibo_URL);
        job.setCode("2641803201");

        DelayRequest delayRequest = new DelayRequest(job,0);
        SpiderEngine.create(weiboOfficialApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<WeiboOfficialLog> weiboOfficialLog = resultItems.get(WeiboOfficialLog.class.getSimpleName());
        Assert.notNull(weiboOfficialLog);

        Request request = resultItems.getRequest();
        Assert.isTrue(Weibo_URL.equals(request.getUrl()));
    }

}
