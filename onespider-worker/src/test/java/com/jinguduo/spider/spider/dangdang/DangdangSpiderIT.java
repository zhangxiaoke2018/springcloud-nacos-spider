package com.jinguduo.spider.spider.dangdang;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.spider.kanmanhua.ComicKanmanhuaSpider;
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
public class DangdangSpiderIT {

    @Autowired
    private DangdangRankSpider spider;


    @Test
    public void testDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://bang.dangdang.com/books/childrensbooks/01.41.48.00.00.00-24hours-0-0-1-1-bestsell");
        job.setCode("cd");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());

        System.out.println(jobs);
    }

}
