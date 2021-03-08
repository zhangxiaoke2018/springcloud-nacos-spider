package com.jinguduo.spider.spider.manmanmanhua;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by lc on 2019/5/20
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ComicMmmhApiSpiderIT {
    @Autowired
    ComicMmmhSpider spider;

    String url = "https://api.manmanapp.com/v3";

    String url2 ="https://api.manmanapp.com/v3?%7B%22worksId%22%3A%221403878%22%2C%22limit%22%3A%223000%22%2C%22api%22%3A%22works%2Findex%22%2C%22token%22%3A%22manmanDefaultToken%22%2C%22info%22%3A%225.0.9_OPPO+R11_android_android_4.4.2_866174010386528_yingyongbao%22%7D";
    @Test
    public void testDetail() throws UnsupportedEncodingException {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        String encode = URLEncoder.encode("{\"limit\":\"20\",\"api\":\"rank-list/list\",\"type\":3,\"token\":\"manmanDefaultToken\",\"page\":\"1\",\"info\":\"5.0.9_OPPO R11_android_android_4.4.2_866174010386528_yingyongbao\"}", "utf-8");
//        job.setUrl(url+"?"+encode);
        job.setUrl(url2);
        job.setMethod("POST");
        job.setCode("code");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addSpiderListeners(new ArrayList(){{
            add(new ComicMmmhDownLoaderListener());

        }}).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
    }
}
