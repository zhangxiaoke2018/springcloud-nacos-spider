package com.jinguduo.spider.spider.fiction;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.webmagic.ResultItems;
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
 * Created by lc on 2019/7/1
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SQSpiderT {
    @Autowired
    SQWebSpider webSpider;
    @Autowired
    SQMobileSpider mobileSpider;
    @Autowired
    SQDetailSpider detailSpider;

    private String url = "http://t.shuqi.com";


    @Test
    public void testVurl(){
        String url = "http://read.xiaoshuo1-sm.com/novel/i.php?interest=女生&p=1&do=is_rank_list&size=200&page=1&type=15&rank=3&month=2019-07";

        Job job = new Job(url);
        job.setCode(Md5Util.getMd5(url));
        job.setPlatformId(CommonEnum.Platform.SQ.getCode());
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(mobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();

    }
    @Test
    public void  test(){
        Job job = new Job(url);
        job.setCode(Md5Util.getMd5(url));
        job.setPlatformId(CommonEnum.Platform.SQ.getCode());
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(webSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> detailJob = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(detailJob, "detailJob is empty.");
        System.out.println(detailJob.size());
        System.out.println(detailJob.get(0).toString());

        job = detailJob.get(0);
        delayRequest = new DelayRequest(job);
        testPipeline = new TestPipeline();
        SpiderEngine.create(mobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        resultItems = testPipeline.getResultItems();
        List<Fiction> fictions = resultItems.get(Fiction.class.getSimpleName());

        System.out.println(fictions.size());
        System.out.println(fictions.get(0));

        detailJob = resultItems.get(Job.class.getSimpleName());

        Job commentAndRateJob = detailJob.stream().filter(p -> p.getUrl().contains("do=sp_get")).findFirst().orElse(null);
        Job detailPageJob = detailJob.stream().filter(p -> p.getUrl().contains("/book/info")).findFirst().orElse(null);


        delayRequest = new DelayRequest(commentAndRateJob);
        testPipeline = new TestPipeline();
        SpiderEngine.create(mobileSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        resultItems = testPipeline.getResultItems();

        List<FictionPlatformRate> rates = resultItems.get(FictionPlatformRate.class.getSimpleName());

        System.out.println(rates.size());
        System.out.println(rates.get(0));

        List<FictionCommentLogs> comments = resultItems.get(FictionCommentLogs.class.getSimpleName());

        System.out.println(comments.size());
        System.out.println(comments.get(0));

        delayRequest = new DelayRequest(detailPageJob);
        testPipeline = new TestPipeline();
        SpiderEngine.create(detailSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        resultItems = testPipeline.getResultItems();
        List<FictionPlatformClick> clicks = resultItems.get(FictionPlatformClick.class.getSimpleName());

        System.out.println(clicks.size());
        System.out.println(clicks.get(0));

    }

}
