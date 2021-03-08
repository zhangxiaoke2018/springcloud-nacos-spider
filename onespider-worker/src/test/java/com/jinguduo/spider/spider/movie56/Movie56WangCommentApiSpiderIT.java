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
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class Movie56WangCommentApiSpiderIT {

    @Autowired
    private Movie56WangCommentApiSpider movie56WangSpider;

    @Test
    public void testContext() {
        Assert.notNull(movie56WangSpider);
    }

    @Test
    public void processNetMovie1()  {

        Job job = new Job(Movie56WangCommentApiSpiderTests.COMMENT_URL);
//        job.setCode("MTM3ODg4Mzgy");
        job.setCode("80681214");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(movie56WangSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get(CommentLog.class.getSimpleName()));
    }
}
