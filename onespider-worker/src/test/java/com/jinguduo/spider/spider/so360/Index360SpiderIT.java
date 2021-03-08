package com.jinguduo.spider.spider.so360;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;



/**
 * Created by lc on 2017/5/4.
 */

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest//6715559

public class Index360SpiderIT {

    @Autowired
    private Index360Spider index360Spider;


    private DelayRequest delayRequest;

    private final Logger log = LoggerFactory.getLogger(Index360SpiderIT.class);

    private final String testUrl = "https://trends.so.com/index/csssprite?q=20200302&area=%E5%85%A8%E5%9B%BD&from=20200517&to=20200520&click=1&t=index";
   // private final String testUrl = "http://trends.so.com/index/csssprite?q=%E4%BA%BA%E6%B0%91%E7%9A%84%E5%90%8D%E4%B9%89&area=%E5%85%A8%E5%9B%BD&from=20170502&to=20170531&click=3&t=media";

    private final String testUrl2 = "http://index.haosou.com/index/soMediaJson?q=赵粤";

    private final String testUrl3 ="https://trends.so.com/index/indexquerygraph?t=30&area=全国&q=隐秘的角落";
    /*
     http://index.haosou.com/result/trend?keywords=%E4%BA%BA%E6%B0%91%E7%9A%84%E5%90%8D%E4%B9%89
     人民的名义
    */
    @Before
    public void setup()  {

        //loading job
        Job job = new Job(testUrl3);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("code");

        //simulate request
        delayRequest = new DelayRequest(job);

    }

    @Test
    public void testInvolveCount() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(index360Spider).addPipeline(testPipeline).addRequest(delayRequest).run();
    }



}
