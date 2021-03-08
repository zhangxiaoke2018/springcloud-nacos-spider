package com.jinguduo.spider.cluster.spider;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
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
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TextSpiderTests {
    
    class TextSpider implements Spider {
        private Site site = SiteBuilder.builder()
                .setDomain("www.baidu.com")
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

    @Test
    public void testRun() throws URISyntaxException, IOException {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setCode("-");
        job.setUrl("https://www.baidu.com/");

        DelayRequest delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(new TextSpider())
                    .addPipeline(testPipeline)
                    .setDownloader(new DownloaderManager())
                    .addRequest(delayRequest)
                    .run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get("text"));
    }
}
