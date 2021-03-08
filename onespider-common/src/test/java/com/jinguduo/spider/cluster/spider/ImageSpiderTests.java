package com.jinguduo.spider.cluster.spider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.cluster.downloader.listener.DownloaderListener;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.Task;;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ImageSpiderTests {
    
    private TestDownloaderListener listener = new TestDownloaderListener();
    
    class ImageSpider implements Spider {
        private Site site = SiteBuilder.builder()
                .setDomain("ss0.bdstatic.com")
                .setCharset("utf-8")
                .addDownloaderListener(listener)
                .build();

        @Override
        public void process(Page page) {
            page.putField("image", page.getBytes());
        }

        @Override
        public Site getSite() {
            return site;
        }
    }
    
    class TestDownloaderListener implements DownloaderListener {
        private int stamp = 0x0;
        @Override
        public void onRequest(HttpClientRequestContext requestContext, Request req, Task task) {
            stamp |= 0x1;
        }

        @Override
        public void onResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
            stamp |= 0x2;
        }
        
        @Override
        public void onError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
            stamp |= 0x4;
        }
        
        public boolean isOk() {
            return (stamp & 0x3) == 3;
        }
    }

    @Ignore("skip")
    @Test
    public void testRun() throws URISyntaxException, IOException {
        Job job = new Job();
        job.setCode("baidu-logo");
        job.setUrl("https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/bd_logo1_31bdc765.png");

        DelayRequest delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(new ImageSpider())
                    .addPipeline(testPipeline)
                    .setDownloader(new DownloaderManager())
                    .addRequest(delayRequest)
                    .run();

        Assert.isTrue(listener.isOk(), "Bad");
        
        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems.get("image"), "Bad");
        
        ByteArrayInputStream ins = new ByteArrayInputStream(resultItems.get("image"));
        BufferedImage img = ImageIO.read(ins);
        File outputfile = new File("/tmp/" + ImageSpiderTests.class.getSimpleName() +".png");
        ImageIO.write(img, "png", outputfile);
    }
}
