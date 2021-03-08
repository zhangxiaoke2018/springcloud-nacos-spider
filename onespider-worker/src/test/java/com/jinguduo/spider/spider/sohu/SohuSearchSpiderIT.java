package com.jinguduo.spider.spider.sohu;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

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
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.pipeline.ConsolePipeline;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class SohuSearchSpiderIT {

    @Autowired
    private SohuSearchSpider sohuSearchSpider;
    
    private static final String MOVIE_URL = "https://so.tv.sohu.com/mts?wd=%E8%8A%B1%E9%AD%81%E4%B9%8B%E6%98%8E%E6%9C%9D%E6%94%BB%E7%95%A5.html";
    private static final String DRAMA_URL = "http://so.tv.sohu.com/list_p1101_p2_p31000_p42017_p5_p6_p73_p8_p91_p10_p11_p12_p13.html";
    private static final String VARIETY_URL = "http://so.tv.sohu.com/list_p1106_p2_p31000_p4_p5_p6_p77_p8_p91_p10_p11_p12_p13.html";

    @Test
    public void findTest()  {
        Job playCountJob = new Job(MOVIE_URL);
        playCountJob.setPlatformId(1);
        playCountJob.setShowId(1);
        playCountJob.setFrequency(100);
        playCountJob.setCode("1652093585");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();

        SpiderEngine.create(sohuSearchSpider).addPipeline(testPipeline).addPipeline(new ConsolePipeline()).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());

        //有可能没有最新的电影
        Assert.notEmpty(shows);
        log.debug(shows.size());
    }
}
