package com.jinguduo.spider.spider.pptv;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by gsw on 2017/02/22.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class PPTvCommentTextSpiderIT {
    @Autowired
    private PPTvCommentTextSpider ppTvCommentTextSpider;

    /**评论文本URL*/
    final String COMMENT_TEXT_URL = "http://apicdn.sc.pptv.com/sc/v3/pplive/ref/vod_25619789/feed/list?appplt=web&action=1&pn=0&ps=20";
    @Test
    public void testContext() {
        Assert.notNull(ppTvCommentTextSpider);
    }


    @Test
    public void processNetDrama()  {
        Job job = new Job("http://apicdn.sc.pptv.com/sc/v3/pplive/ref/vod_25112099/feed/list?appplt=web&action=1&pn=10&ps=20");
        job.setCode("111111");
        job.setMethod("GET");
        job.setPlatformId(11);
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(ppTvCommentTextSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTexts = resultItems.get(CommentText.class.getSimpleName());
        List<Job> nextJob = resultItems.get(Job.class.getSimpleName());
    }
}
