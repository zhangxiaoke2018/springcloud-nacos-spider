package com.jinguduo.spider.spider.letv;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.ResultItems;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * created by gsw 2017-02-20
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class LeCommentSpiderIT {

    @Autowired
    private LeCommentSpider leCommentSpider;

    final static String URL = "http://api.my.le.com/vcm/api/list?rows=20&page=355&listType=1&xid=28802070&pid=10034828";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");
        job.setCode("27639052");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void getCommentContent()  {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(leCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTexts = resultItems.get(CommentText.class.getSimpleName());
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(CollectionUtils.isNotEmpty(commentTexts));
        Assert.notNull(job.get(0).getUrl());

    }
}
