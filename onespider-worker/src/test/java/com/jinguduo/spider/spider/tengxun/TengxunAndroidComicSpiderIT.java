package com.jinguduo.spider.spider.tengxun;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.SpiderListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lc on 2019/3/28
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunAndroidComicSpiderIT {

    @Autowired
    TengxunAndroidComicSpider spider;

    private DelayRequest delayRequest;


    private static final String url = "http://android.ac.qq.com/7.21.3/Comic/comicDetail/comic_id/549715";

    @Before
    public void setup()  {

        //loading job
        Job job = new Job(url);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("code");

        //simulate request
        delayRequest = new DelayRequest(job);

    }

    @Test
    public void test(){
        List<SpiderListener> list = new ArrayList<>();

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest)
                .addSpiderListeners(Lists.newArrayList(new TengxunAndroidComicDownLoaderListener()))
                .run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);
    }
}
