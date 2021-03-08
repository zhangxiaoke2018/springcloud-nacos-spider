package com.jinguduo.spider.spider.movie56;



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
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/15 下午3:11
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class Movie56WangPCSpiderIT {

    @Autowired
    private Movie56WangPCSpider movie56WangPCSpider;

    @Test
    public void testContext() {
        Assert.notNull(movie56WangPCSpider);
    }

    @Test
    public void processPC()  {
        Job job = new Job("http://vstat.v.blog.sohu.com/dostat.do?method=getVideoPlayCount&v=82654202");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(movie56WangPCSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()));


    }

}
