package com.jinguduo.spider.spider.kankan;


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
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class KanKanCommentApiSpiderIT {
    @Autowired
    private KanKanCommentApiSpider kanKanCommentApiSpider;

    @Test
    public void testContext() {
        Assert.notNull(kanKanCommentApiSpider);
    }

    @Test
    public void testCommentCountCapture()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://api.t.kankan.com/weibo_list_vod.json?jsobj=hotscomment&hot=1&movieid=68810");//KanKanCommentApiSpiderTests.COMMENT_URL);
        job.setCode("68810");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(kanKanCommentApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get(CommentLog.class.getSimpleName()));
    }

    /**评论文本测试*/
    @Test
    public void testCommentTextCapture()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://api.t.kankan.com/weibo_list_vod.json?movieid=6904&perpage=25&page=1");
        job.setCode("6904");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(kanKanCommentApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTexts = resultItems.get(CommentText.class.getSimpleName());
        Assert.notNull(commentTexts);
        Assert.isTrue(commentTexts.size()==25);
        List<Job> nextJob = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(nextJob);
    }

}
