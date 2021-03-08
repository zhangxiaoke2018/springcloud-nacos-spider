package com.jinguduo.spider.spider.douban;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import com.jinguduo.spider.common.proxy.FastProxyPool;
import com.jinguduo.spider.common.proxy.ProxyPool;
import com.jinguduo.spider.data.table.DoubanLog;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.service.QiniuService;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DoubanMovieSpiderHandWork {

    @Autowired
    @InjectMocks
    private DoubanMovieSpider doubanMovieSpider;
    
    //@Autowired
    //private ProxyPoolManager proxyPoolManager;
    
    @Autowired
    private DistributedScheduler scheduler;

    final static String[] DOUBAN_URL_ARRAY = new String[]{
            "https://movie.douban.com/subject/26817017/#gd-showname=8a661905ad85290ab1067a04e5afb96a",
            "https://movie.douban.com/subject/26859982/", //电视剧
            "https://movie.douban.com/subject/26260853/", //电影
            "https://movie.douban.com/subject/26757373/", //
            "https://movie.douban.com/subject/26732535/", //
            "https://movie.douban.com/subject/27097954/",
    };

    @Mock
    private QiniuService qiniuService;

    @Before
    public void setup() throws FileNotFoundException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(qiniuService.upload(Mockito.anyString())).thenReturn("http://www.xxxxxx.com");
    }
    
    //@Ignore("handwork")
    @Test
    public void testCookieAndProxy()  {
        TestPipeline testPipeline = new TestPipeline();
        
        SpiderEngine engine = SpiderEngine.create(doubanMovieSpider)  
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
        
        Site site = doubanMovieSpider.getSite();
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

        List<DoubanLog> doubanLogs = resultItems.get(DoubanLog.class.getSimpleName());
        Assert.notEmpty(doubanLogs, "bad");
    }
}
