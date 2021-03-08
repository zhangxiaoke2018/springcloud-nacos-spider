package com.jinguduo.spider.spider.letv;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class LeSearchSpiderIT {

    @Autowired
    private LeSearchSpider leSearchSpider;
    
    private static final String MOVIE_URL = "http://list.le.com/listn/c1_t-1_a-1_y-1_s5_lg-1_ph-1_md_o1_d1_p.html";
    private static final String DRAMA_URL = "http://list.le.com/listn/c2_t-1_a50001_y2017_s4_md_o51_d2_p.html";

    @Test
    public void findTest()  {

        Job playCountJob = new Job(DRAMA_URL);
        playCountJob.setPlatformId(1);
        playCountJob.setShowId(1);
        playCountJob.setFrequency(100);
        playCountJob.setCode("1652093585");

        DelayRequest delayRequest = new DelayRequest(playCountJob);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(leSearchSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        log.debug(shows.size());
    }
}
