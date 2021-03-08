package com.jinguduo.spider.spider.premproxy;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class PermProxySpiderIT {

    @Autowired
    private PremProxySpider premProxySpider;
    
    @Test
    public void testContext() {
    	Assert.notNull(premProxySpider, "Bad");
    }
    
    // premproxy.com 被GFW墙
    //@Test
    public void testGetProxyList() {
        TestPipeline testPipeline = new TestPipeline();
        
        Job job = new Job();
        job.setUrl("https://premproxy.com/socks-by-country/China-01.htm");
        job.setCode("premproxy-china");
        DelayRequest request = new DelayRequest(job);
        
        SpiderEngine.create(premProxySpider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(request)
            .run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "bad");
        List<Proxy> proxies = resultItems.get(Proxy.class.getSimpleName());
        Assert.notEmpty(proxies, "bad");
    }
}
