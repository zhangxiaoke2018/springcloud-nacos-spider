package com.jinguduo.spider.spider.dangdang;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.webmagic.ResultItems;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DangdangDetailSpiderIT {

    @Autowired
    private DangdangDetailSpider spider;


    @Test
    public void testDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        //http://product.dangdang.com/27946916.html
        //
        job.setUrl("http://product.dangdang.com/index.php?r=comment%2Flist&productId=27946916&mainProductId=27946916");
        job.setCode("23428051");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());

        System.out.println(jobs);
    }

}
