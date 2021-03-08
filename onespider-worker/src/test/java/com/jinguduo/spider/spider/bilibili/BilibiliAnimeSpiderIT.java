package com.jinguduo.spider.spider.bilibili;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ShowLog;
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
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 2016/10/25 下午2:43
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class BilibiliAnimeSpiderIT {

    @Autowired
    private BiliBiliAnimeSpider biliAnimeSpider;

    @Test
    public void testContext() {
        Assert.notNull(biliAnimeSpider);
    }

    @Test
    public void testProcessNewMovie()  {


        Job j = new Job("http://bangumi.bilibili.com/anime/6159");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliAnimeSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);
        System.out.println(job.get(0).getUrl());

    }

    @Test
    public void testProcessAnime()  {
        Job j = new Job("http://bangumi.bilibili.com/jsonp/seasoninfo/5570.ver?callback=seasonListCallback&jsonp=jsonp");
        j.setPlatformId(1);
        j.setShowId(1);
        j.setCode("iaUkysRlic7y2QDnY");
        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliAnimeSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLog);
        System.out.println(showLog.get(0).getPlayCount());
        Assert.isTrue(showLog.get(0).getPlayCount()>0L);
    }

    @Test
    public void getSourceTest(){
        Job job = new Job("http://bangumi.bilibili.com/web_api/get_source?episode_id=97036&csrf=", "POST");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setCode("23456789");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliAnimeSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
    }
    @Test
    public void getAnimePlayCount(){
        Job job = new Job("http://bangumi.bilibili.com/ext/web_api/season_count?season_id=5852&season_type=4");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setCode("23456789");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliAnimeSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs);
    }
    @Test
    public void creatJobTest(){
        Job job = new Job("http://bangumi.bilibili.com/anime/5554");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliAnimeSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> showLogs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(showLogs);
    }

}
