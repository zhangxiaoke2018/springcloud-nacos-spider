package com.jinguduo.spider.spider.youku;

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
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * Created by gsw on 2017/2/23.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YouKuCommentTextSpiderIT {

    @Autowired
    private YoukuCommentTextSpider youkuCommentTextSpider;

    @Test
    public void testContext() {
        Assert.notNull(youkuCommentTextSpider);
    }

    @Test
    public void testCommentContent() {
        Job job = new Job("http://api.mobile.youku.com/video/comment/list/new?vid=652366963&pl=30&pg=34");
        job.setCode("XMjUxMDkzOTkyNA==");

        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuCommentTextSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notEmpty(commentTextList);
        List<Job> nextJob = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(nextJob.get(0).getUrl().equals("http://api.mobile.youku.com/video/comment/list/new?vid=652366963&pl=30&pg=35"));
    }

}
