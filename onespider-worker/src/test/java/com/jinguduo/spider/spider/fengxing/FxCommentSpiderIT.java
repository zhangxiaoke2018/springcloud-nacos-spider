package com.jinguduo.spider.spider.fengxing;



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
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class FxCommentSpiderIT {

    @Autowired
    private FxCommentSpider fxCommentSpider;

    @Test
    public void testContext() {
        Assert.notNull(fxCommentSpider);
    }

    @Test
    public void testCommentCountCapture()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        //下面的测试链接404了
        job.setUrl(FxCommentSpiderTests.COMMENT_URL);
        DelayRequest delayRequest = new DelayRequest(job);
        job.setCode("g-312359");
        SpiderEngine.create(fxCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get(CommentLog.class.getSimpleName()));

    }
}
