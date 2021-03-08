package com.jinguduo.spider.spider.kuaidaili;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.ResultItems;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class KuaidailiSpiderIT {

    @Autowired
    private KuaidailiSpider spider;

    @Test
    public void testInvolveCount() {
    	Job job = new Job("http://www.kuaidaili.com/free/inha/1/");
    	job.setCode("code1");
    	
    	DelayRequest delayRequest = new DelayRequest(job);
    	
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline)
                .setDownloader(new DownloaderManager())
                .addRequest(delayRequest).run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "bad");
        
        List<Proxy> proxies = resultItems.get(Proxy.class.getSimpleName());
        Assert.notEmpty(proxies, "bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
    }

}
