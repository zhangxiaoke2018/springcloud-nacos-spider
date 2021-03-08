package com.jinguduo.spider.spider.sohu;


import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class SohuDramaSpiderIT {
    @Autowired
    private SohuDramaSpider sohuDramaSpider;

    @Test
    public void testPlaylist() {

        /** 幻城剧集api */
        String VIDEO_LIST_URL = "http://pl.hd.sohu.com/videolist?playlistid=9393904&order=0&cnt=1&callback=__get_videolist";
        
        //loading job
        Job job = new Job(VIDEO_LIST_URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        
        //simulate request
        DelayRequest delayRequest_vlist = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuDramaSpider).addPipeline(testPipeline).addRequest(delayRequest_vlist).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "Bad!");
        Assert.notNull(resultItems.get(Show.class.getSimpleName()), "Bad!");
        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(jobList.size() > 0, "Bad!");
    }

    @Test
    public void testPlaylist2() {

        String VIDEO_LIST_URL = "http://pl.hd.sohu.com/videolist?playlistid=9457553&order=0&cnt=1&callback=__get_videolist";
        
        //loading job
        Job job = new Job(VIDEO_LIST_URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        
        //simulate request
        DelayRequest delayRequest_vlist = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuDramaSpider).addPipeline(testPipeline).addRequest(delayRequest_vlist).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "Bad!");
        Assert.notNull(resultItems.get(Show.class.getSimpleName()), "Bad!");
        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(jobList.size() > 0, "Bad!");
    }

    @Test
    public void testPlaylist3() {

        String VIDEO_LIST_URL = "http://pl.hd.sohu.com/videolist?playlistid=9402322&order=0&cnt=1&callback=__get_videolist";
        
        //loading job
        Job job = new Job(VIDEO_LIST_URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        
        //simulate request
        DelayRequest delayRequest_vlist = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuDramaSpider).addPipeline(testPipeline).addRequest(delayRequest_vlist).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "Bad!");
        Assert.notNull(resultItems.get(Show.class.getSimpleName()), "Bad!");
        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(jobList.size() > 0, "Bad!");
        for (Job j : jobList) {
            Assert.notNull(j.getCode(), "Bad");
        }
    }
}


