package com.jinguduo.spider.spider.taomi;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.spider.bilibili.BiliBiliAnimeSpider;
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

public class TaomiAnimeSpiderIT {

    @Autowired
    private TaomiAnimeSpider taomiAnimeSpider;

    @Test
    public void testContext() {
        Assert.notNull(taomiAnimeSpider);
    }

    @Test
    public void testProcessNewMovie()  {

        Job j = new Job("http://v.61.com/comic/10224/");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(taomiAnimeSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);
        Assert.isTrue("http://vapp.61.com/api.php?method=api.Score.getVideoInfo&vid=10224".equals(job.get(0).getUrl()));
        System.out.println(job.get(0).getUrl());

    }

}
