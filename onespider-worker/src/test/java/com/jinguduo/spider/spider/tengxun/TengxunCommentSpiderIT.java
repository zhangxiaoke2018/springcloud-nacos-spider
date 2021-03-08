package com.jinguduo.spider.spider.tengxun;


import java.util.List;

import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
public class TengxunCommentSpiderIT {

    @Autowired
    private TengxuntvCommentSpider tengxuntvCommentSpider;

    private final static String COMMENT_NUM_URL = "http://coral.qq.com/article/2604298936/commentnum";
    private final static String COMMENT_URL = "https://coral.qq.com/article/1439447313/comment?commentid=&reqnum=20";
    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(COMMENT_NUM_URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(tengxuntvCommentSpider);
    }


    @Test
    public void testRun() {
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxuntvCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLog);
    }

    /**
     * 评论文本测试
     */
    @Test
    public void testCommentText()  {
        Job job = new Job(COMMENT_URL);
        job.setCode("1439447313");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setMethod("GET");

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tengxuntvCommentSpider).addPipeline(testPipeline).addRequest(new DelayRequest(job)).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTexts = resultItems.get(CommentText.class.getSimpleName());
        Assert.notNull(commentTexts);

    }

}
