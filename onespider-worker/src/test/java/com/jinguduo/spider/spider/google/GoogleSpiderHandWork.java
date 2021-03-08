package com.jinguduo.spider.spider.google;

import org.apache.commons.lang3.RandomStringUtils;
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
import com.jinguduo.spider.cluster.scheduler.DistributedScheduler;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.common.proxy.FastProxyPool;
import com.jinguduo.spider.common.proxy.ProxyPool;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class GoogleSpiderHandWork {

    class GoogleSpider implements Spider {
        private Site site = SiteBuilder.builder()
                .setDomain("www.google.com")
                .setCharset("utf-8")
                .build();

        @Override
        public void process(Page page) {
            page.putField("text", page.getRawText());
        }

        @Override
        public Site getSite() {
            return site;
        }
    }
    
    @Autowired
    private DistributedScheduler scheduler;

    final static String[] DOUBAN_URL_ARRAY = new String[]{
            "https://www.google.com/",
            "https://www.google.com.sg/",
            "https://www.google.com.hk/",
            "https://www.google.co.jp/",
    };
    
    @Test
    public void testSocksProxy()  {
        TestPipeline testPipeline = new TestPipeline();
        
        GoogleSpider googleSpider = new GoogleSpider();
        SpiderEngine engine = SpiderEngine.create(googleSpider)  
                .addPipeline(testPipeline)
                .setScheduler(scheduler)
                .setDownloader(new DownloaderManager());
        
        // add job
        for (int i = 0; i < DOUBAN_URL_ARRAY.length; i++) {
            Job job = new Job();
            job.setUrl(DOUBAN_URL_ARRAY[i]);
            job.setCode(RandomStringUtils.randomAlphanumeric(8));
            engine.addRequest(new DelayRequest(job));
        }
        
        Site site = googleSpider.getSite();
        site.setCycleRetryTimes(1);
        site.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/602.2.14 (KHTML, like Gecko) Version/10.0.1 Safari/602.2.14");
        //site.addCookie("douban.com", "ll", "0");
        //site.addCookie("douban.com", "bid", "SDjiSSpvM20");
        
        // proxy
        //site.setHttpProxyPool(proxyPoolManager.getHttpProxyPool());
        
        /**/
        ProxyPool proxyPool = new FastProxyPool();
        site.setProxyPool(proxyPool);
        Proxy socksProxy = Proxy.newSocks5Proxy();
        socksProxy.setHost("127.0.0.1:1086");
        //socksProxy.setHost("61.50.244.180:1080");
        //socksProxy.setHost("61.135.155.82:1080");
        //socksProxy.setHost("183.239.240.138:1080");
        proxyPool.addProxy(socksProxy);
        
        /**
        httpProxy.setHost("211.154.8.56:8888");  // zxh
        httpProxy.setHost("211.154.8.38:8888");
        //httpProxy.setHost("222.82.222.242:9999");
        proxyPool.addProxy(httpProxy);
        /**/
        
        engine.thread(2).run();

        ResultItems resultItems = testPipeline.getResultItems();

        Assert.notNull(resultItems, "bad");
    }
}
