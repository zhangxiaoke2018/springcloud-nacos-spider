package com.jinguduo.spider.spider.bilibili;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;


import com.jinguduo.spider.data.table.BilibiliFansCount;
import com.jinguduo.spider.data.table.BilibiliVideoScore;
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
public class BiliBiliPlayPageSpiderIT {

    @Autowired
    BiliBiliPlayPageSpider biliBiliPlayPageSpider;

    @Test
    public void testPlayPage(){
        Job job = new Job("https://www.bilibili.com/bangumi/media/md6159/");

        job.setPlatformId(24);
        job.setShowId(1);
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliBiliPlayPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobList, "未生成评论和弹幕job");
    }

    @Test
    public void testAnime(){
        Job job = new Job("https://www.bilibili.com/bangumi/play/ep105246");
        job.setPlatformId(1);
        job.setShowId(1);
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliBiliPlayPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobList, "未生成播放量任务");
        List<BilibiliVideoScore> bilibiliVideoScoreList=resultItems.get(BilibiliVideoScore.class.getSimpleName());
        Assert.notEmpty(bilibiliVideoScoreList,"pingfen not null");
    }

    @Test
    public void testScore(){
        Job job=new Job("https://www.bilibili.com/bangumi/media/md103172/?spm_id_from=666.10.bangumi_detail.2");
        DelayRequest delayRequest=new DelayRequest(job);
        TestPipeline testPipeline=new TestPipeline();
        SpiderEngine.create(biliBiliPlayPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems=testPipeline.getResultItems();
        List<BilibiliVideoScore> bilibiliVideoScores=resultItems.get(BilibiliVideoScore.class.getSimpleName());
        Assert.isTrue(bilibiliVideoScores.size() > 0);
        List<Job> jobList=resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobList,"未生成任务");
    }

    @Test
    public void testFans(){
        Job job = new Job("https://www.bilibili.com/bangumi/media/md101852/?spm_id_from=666.10.b_62616e67756d695f64657461696c.2");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setCode("md101852");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliBiliPlayPageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        Object o = resultItems.get(BilibiliFansCount.class.getSimpleName());
        Assert.notNull(o,"未拿到追番人数");
    }
}
