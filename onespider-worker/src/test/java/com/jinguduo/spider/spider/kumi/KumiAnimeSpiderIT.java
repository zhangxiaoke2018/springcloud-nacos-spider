package com.jinguduo.spider.spider.kumi;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.spider.taomi.TaomiAnimeSpider;
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
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 2016/10/25 下午2:43
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class KumiAnimeSpiderIT {

    @Autowired
    private KumiAnimeSpider kumiAnimeSpider;

    @Test
    public void testContext() {
        Assert.notNull(kumiAnimeSpider);
    }

    @Test
    public void testProcessNewMovie()  {

        Job j = new Job("http://www.kumi.cn/donghua/85063.html");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(kumiAnimeSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(job);

    }

}
