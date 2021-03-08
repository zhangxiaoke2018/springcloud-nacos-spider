package com.jinguduo.spider.spider.douban;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.DoubanCommentsText;
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
 * Created by lc on 2020/1/13
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DoubanBookSpiderIT {


    private static final String  SEARCH_URL= "https://search.douban.com/book/subject_search?search_text=9780439739511&cat=1001";

    private static final String  DETAIL_URL= "https://book.douban.com/subject/2117875/";


    @Autowired
    DoubanBookSpider dbSpider;
    @Autowired
    DoubanBookSearchSpider searchSpider;


    @Test
    public void testSearch(){
        TestPipeline pipeline = new TestPipeline();
        Job job = new Job(SEARCH_URL);
        job.setCode("20538060");
        job.setParentCode("9780439739511");
        job.setPlatformId(59);
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(searchSpider).addPipeline(pipeline).addRequest(delayRequest).run();
        ResultItems resultItems = pipeline.getResultItems();
        System.out.println("DEBUG BLOCK POINT");
    }

    @Test
    public void testDetail(){
        TestPipeline pipeline = new TestPipeline();
        Job job = new Job(DETAIL_URL);
        job.setCode("cd");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(dbSpider).addPipeline(pipeline).addRequest(delayRequest).run();
        ResultItems resultItems = pipeline.getResultItems();
        System.out.println("DEBUG BLOCK POINT");
    }



}
