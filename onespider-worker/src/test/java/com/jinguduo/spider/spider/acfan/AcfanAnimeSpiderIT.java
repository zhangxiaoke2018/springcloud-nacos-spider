package com.jinguduo.spider.spider.acfan;


import java.util.List;

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

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 2016/10/25 下午2:43
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class AcfanAnimeSpiderIT {

    @Autowired
    private AcfanAnimeSpider acfanAnimeSpider;

    @Test
    public void testContext() {
        Assert.notNull(acfanAnimeSpider);
    }

    @Test
    public void testProcessAnime() {

        Job j = new Job("http://www.acfun.cn/v/ab1470468_1");
        j.setPlatformId(1);
        j.setShowId(1);
        j.setCode("1470468");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(acfanAnimeSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);
        Assert.isTrue("http://www.acfun.cn/bangumi/count/bangumi_view.aspx?bangumiId=1470468".equals(job.get(0).getUrl()));
    }
    @Test
    public void testProcessPlayCount()  {
        Job j = new Job("http://www.acfun.cn/bangumi/count/bangumi_view.aspx?bangumiId=1470468");
        j.setPlatformId(1);
        j.setShowId(1);
        j.setCode("1470468");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(acfanAnimeSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLog);
        Assert.isTrue(showLog.get(0).getPlayCount()>0L);
    }

}
