package com.jinguduo.spider.spider.manmanmanhua;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

/**
 * Created by lc on 2019/5/20
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ComicMmmhPcSpiderIT {
    @Autowired
    ComicMmmhPcSpider spider;

    String url = "https://m.manmanapp.com/works/comic-list-ajax.html?id=1403896&sort=1&page=1";

    @Test
    public void testDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(url);
        job.setMethod("POST");
        job.setCode("1403734");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addSpiderListeners(new ArrayList(){{
            add(new ComicMmmhPcDownLoaderListener());

        }}).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
    }
}
