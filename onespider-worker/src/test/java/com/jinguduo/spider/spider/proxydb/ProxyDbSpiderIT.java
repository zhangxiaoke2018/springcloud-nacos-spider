package com.jinguduo.spider.spider.proxydb;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
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
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ProxyDbSpiderIT {
    
    @Autowired
    private ProxyDbSpider proxyDbSpider;
    
    @Before
    public void setup() {
        Site site = proxyDbSpider.getSite();
        site.setSleepTime(0);
        site.setTimeOut((int)TimeUnit.SECONDS.toMillis(30));
        site.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.1.2 Safari/603.3.8");
    }

    @Test
    public void testSocks5Page() {
        TestPipeline testPipeline = new TestPipeline();
        
        Job job = new Job();
        job.setUrl("http://proxydb.net/?protocol=socks5&exclude_gateway=1&country=CN");
        job.setCode("sock5");
        DelayRequest request = new DelayRequest(job);
        
        SpiderEngine.create(proxyDbSpider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(request)
            .run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "bad");
        List<Proxy> proxies = resultItems.get(Proxy.class.getSimpleName());
        Assert.notEmpty(proxies, "bad");
    }
    
    @Test
    public void testSocks4Page() {
        TestPipeline testPipeline = new TestPipeline();
        
        Job job = new Job();
        job.setUrl("http://proxydb.net/?protocol=socks4&exclude_gateway=1&country=CN");
        job.setCode("sock4");
        DelayRequest request = new DelayRequest(job);
        
        SpiderEngine.create(proxyDbSpider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(request)
            .run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "bad");
        List<Proxy> proxies = resultItems.get(Proxy.class.getSimpleName());
        Assert.notEmpty(proxies, "bad");
    }
    
    @Test
    public void testHttpPage() {
        TestPipeline testPipeline = new TestPipeline();
        
        Job job = new Job();
        job.setUrl("http://proxydb.net/?protocol=http&anonlvl=2&anonlvl=3&anonlvl=4&exclude_gateway=1&country=CN");
        job.setCode("http");
        DelayRequest request = new DelayRequest(job);
        
        SpiderEngine.create(proxyDbSpider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(request)
            .run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "bad");
        List<Proxy> proxies = resultItems.get(Proxy.class.getSimpleName());
        Assert.notEmpty(proxies, "bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
    }
    
    @Test
    public void testHttpPage2() {
        TestPipeline testPipeline = new TestPipeline();
        
        Job job = new Job();
        job.setUrl("http://proxydb.net/?protocol=http&anonlvl=2&anonlvl=3&anonlvl=4&exclude_gateway=1&country=CN&offset=20");
        job.setCode("http");
        DelayRequest request = new DelayRequest(job);
        
        SpiderEngine.create(proxyDbSpider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(request)
            .run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "bad");
        List<Proxy> proxies = resultItems.get(Proxy.class.getSimpleName());
        Assert.notEmpty(proxies, "bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
    }
    
    @Test
    public void testHttpsPage() {
        TestPipeline testPipeline = new TestPipeline();
        
        Job job = new Job();
        job.setUrl("http://proxydb.net/?protocol=https&anonlvl=2&anonlvl=3&anonlvl=4&exclude_gateway=1&country=CN");
        job.setCode("http");
        DelayRequest request = new DelayRequest(job);
        
        SpiderEngine.create(proxyDbSpider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(request)
            .run();
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "bad");
        List<Proxy> proxies = resultItems.get(Proxy.class.getSimpleName());
        Assert.notEmpty(proxies, "bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
    }
}
