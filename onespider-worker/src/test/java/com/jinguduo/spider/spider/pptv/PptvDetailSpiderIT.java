package com.jinguduo.spider.spider.pptv;


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
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/7/19 下午3:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class PptvDetailSpiderIT {

    @Autowired
    private PptvDetailSpider pptvDetailSpider;

    @Test
    public void testPptvDetail()  {
        //http://v.pptv.com/show/4EYysBhib7iayPDXU.html 网剧
        //http://v.pptv.com/show/tzIZlv5k1BJ181s.html?rcc_src=S1 电影合集
        //http://v.pptv.com/show/HyEIhib9VxQNm5Ew.html?rcc_src=P4 单集电影
        //http://v.pptv.com/show/LfTtatI4qOZJxy8.html?rcc_src=S1
        Job job = new Job("http://v.pptv.com/show/iavuQD3fdTYvubNQ.html");
        job.setPlatformId(11);
        job.setShowId(1);
        job.setCode("iavuQD3fdTYvubNQ");

        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();

        SpiderEngine.create(pptvDetailSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job2 = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(job2);
    }
}
