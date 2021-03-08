package com.jinguduo.spider.spider.iqiyi;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.ResultItems;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class iqiyiEpisodePaopaoCommentSpiderIT {

    @Autowired
    private IqiyiEpisodePaopaoCommentSpider iqiyiCommentSpider;

    @Test
    public void testCaptureCommentCount()  {
        Job job = new Job();
        job.setUrl("https://sns-comment.iqiyi.com/v3/comment/get_comments.action?agent_type=118&agent_version=9.11.5&authcookie=null&business_type=17&content_id=2773667700&hot_size=10&last_id=&page=1&page_size=10&types=hot,time&callback=jsonp_1551681600905_43514");
        job.setCode("2773667700");
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(iqiyiCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();

        List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notEmpty(commentLog, "Bad");

//        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
//        Assert.notEmpty(jobs, "Bad");
    }

}
