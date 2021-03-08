package com.jinguduo.spider.spider.letv;

import java.util.List;

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
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
public class LeBannerSpiderIT {

    @Autowired
    private LeBannerSpider leBannerSpider;
    
    @Before
    public void test(){
        Assert.notNull(leBannerSpider);
    }
    
    @Test
    public void testBannerDrama(){
        Job j = new Job("http://tv.le.com/");
        j.setCode("test");
        
        DelayRequest d = new DelayRequest(j);
        TestPipeline t = new TestPipeline();
        SpiderEngine.create(leBannerSpider).addPipeline(t).addRequest(d).run();
        
        ResultItems resultItems = t.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        Assert.notNull(jobs);
        Assert.notNull(jobs);
        shows.forEach(s->log.debug(s.toString()));
    }
}
