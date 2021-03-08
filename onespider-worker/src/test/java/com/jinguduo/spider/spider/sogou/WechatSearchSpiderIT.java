package com.jinguduo.spider.spider.sogou;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class WechatSearchSpiderIT {
    @Autowired
    private WeixinSearchSpider weixinSearchSpider;
    public final static String URL = "https://weixin.sogou.com/weixin?type=2&query=杨幂&ie=utf8&s_from=input&_sug_=n&_sug_type_=1";

    @Test
    public void testProcess() {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(URL);
        job.setCode("");
        DelayRequest delayRequest = new DelayRequest(job);

        List<SpiderListener> list = new ArrayList<>();
        list.add(new WechatCookieDownloaderListener());

//        for (int i = 0; i < 100; i++) {
            SpiderEngine.create(weixinSearchSpider)
                    .setDownloader(new DownloaderManager())
                    .addSpiderListeners(list)
                    .addPipeline(testPipeline)
                    .addRequest(delayRequest)
                    .run();
            ResultItems resultItems = testPipeline.getResultItems();

//            System.out.println("--------------------------------------" + i + "====================================");
//        }



//        Request request = resultItems.getRequest();
//        Assert.isTrue(URL.equals(request.getUrl()));
        System.out.println("%%%%%%%%%%%%%%%%%test end%%%%%%%%%%%%%%%%%%%%%%");
    }
}
