package com.jinguduo.spider.spider.iqiyi;


import java.util.List;

import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;

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
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiCommentSpiderIT {

    @Autowired
    private IqiyiCommentSpider iqiyiCommentSpider;

    @Test
    public void testCaptureCommentCount()  {
        Job job = new Job();
        job.setUrl("http://api.t.iqiyi.com/qx_api/comment/get_video_comments?need_total=1&page=1&page_size=30&page_size_reply=3&qypid=01010011010000000000&sort=hot&tvid=2797335900");
        job.setCode("2797335900");
        DelayRequest delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(iqiyiCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notEmpty(commentLog, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testCaptureCommentContentForOld()  {
        Job job = new Job();
        job.setUrl("http://api.t.iqiyi.com/qx_api/comment/get_video_comments?need_total=1&page=100&page_size=30&page_size_reply=3&qypid=01010011010000000000&sort=hot&tvid=633977700");
        job.setCode("504028400");
        DelayRequest delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(iqiyiCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notEmpty(commentTextList, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }

    @Test
    public void testCapturePopCommentContent() {
        Job job = new Job("http://api.t.iqiyi.com/feed/get_feeds?agenttype=118&wallId=213497447&feedTypes=1%2C7&count=20&top=1&baseTvId=641851900&feedId=");
        job.setFrequency(9871);  // for test
        job.setCode("619723000");
        DelayRequest delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(iqiyiCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notEmpty(commentTextList, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
        Assert.isTrue(FrequencyConstant.COMMENT_TEXT == jobs.get(0).getFrequency(), "Comment Text Job Frequency is bad");
    }
    
    
    @Test
    public void testCaptureCommentReplyForOld()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://api.t.iqiyi.com/qx_api/comment/get_comment_with_repies?contentid=27860650948&escape=true&need_reply=true&page=1&page_size=10&sort=hot");
        job.setCode("31960567648");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(iqiyiCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notEmpty(commentTextList, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testCapturePopCommentReply()  {
        Job job = new Job();
        job.setUrl("http://api.t.iqiyi.com/feed/get_comments?contentid=27882842948&page_size=5&page=1");
        job.setCode("31960567648");
        DelayRequest delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(iqiyiCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notEmpty(commentTextList, "Bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
}
