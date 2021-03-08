package com.jinguduo.spider.cluster.engine;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.downloader.Downloader;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;
import com.jinguduo.spider.webmagic.scheduler.Scheduler;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SpiderEngineTests {
	
	@Test
	public void testBadHttpResponseException() {
		// job
        Job job = new Job("http://SpiderEngineTests.com/test");
        job.setCode("test");
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.FORBIDDEN_403);
        page.setRawText("BadHttpResponse");
        // spider
        TestSpider testSpider = new TestSpider();
        int cycleRetryTimes = 3;
        testSpider.site.setCycleRetryTimes(cycleRetryTimes);
        
        AtomicInteger counter = new AtomicInteger(0);
        
        SpiderEngine engine = SpiderEngine.create(testSpider)
        	.addPipeline(new TestPipeline())
        	.setDownloader(new Downloader() {
                @Override
                public Page download(Request request, Task task) {
                	counter.incrementAndGet();
                    return page;
                }

                @Override
                public void setThread(int threadNum) {
                	// no op
                }
            })
        	.addRequest(delayRequest);
        
        engine.run();
        
        Assert.isTrue(counter.intValue() >= cycleRetryTimes, "Bad");
	}
	
	@Test
	public void testDownloaderFail() {
		// job
        Job job = new Job("http://SpiderEngineTests.com/test");
        job.setCode("test");
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // spider
        TestSpider testSpider = new TestSpider();
        int cycleRetryTimes = 3;
        testSpider.site.setCycleRetryTimes(cycleRetryTimes);
        
        AtomicInteger counter = new AtomicInteger(0);
        
        SpiderEngine engine = SpiderEngine.create(testSpider)
        	.addPipeline(new TestPipeline())
        	.setDownloader(new Downloader() {
                @Override
                public Page download(Request request, Task task) {
                	counter.incrementAndGet();
                    return Page.fail();
                }

                @Override
                public void setThread(int threadNum) {
                	// no op
                }
            })
        	.addRequest(delayRequest);
        
        engine.run();
        
        Assert.isTrue(counter.intValue() >= cycleRetryTimes, "Bad");
	}
	
	@Test
	public void testDownloaderThrowUncheckedException() {
		// job
        Job job = new Job("http://SpiderEngineTests.com/test");
        job.setCode("test");
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // spider
        TestSpider testSpider = new TestSpider();
        int cycleRetryTimes = 3;
        testSpider.site.setCycleRetryTimes(cycleRetryTimes);
        
        AtomicInteger counter = new AtomicInteger();
        
        SpiderEngine engine = SpiderEngine.create(testSpider)
        	.addPipeline(new TestPipeline())
        	.setDownloader(new Downloader() {
                @Override
                public Page download(Request request, Task task) {
                	counter.incrementAndGet();
                	throw new RuntimeException("");
                }

                @Override
                public void setThread(int threadNum) {
                	// no op
                }
            })
        	.addRequest(delayRequest);
        
        engine.run();
        
        Assert.isTrue(counter.intValue() >= cycleRetryTimes, "Bad");
	}

    @Ignore("long time")
    @Test
    public void testStartAndStop() throws InterruptedException {
    	SpiderEngine spider = SpiderEngine.create(new TestSpider()).addPipeline(new Pipeline() {
            @Override
            public void process(ResultItems resultItems, Task task) {
                System.out.println(1);
            }
        }).thread(1);
        spider.start();
        Thread.sleep(1000);
        spider.stop();
        Thread.sleep(1000);
        spider.start();
        Thread.sleep(1000);
    }
    
    @Ignore("long time")
    @Test
    public void testWaitAndNotify() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("round " + i);
            testRound();
        }
    }

    private void testRound() {
    	SpiderEngine spider = SpiderEngine.create(new Spider() {

            @Override
            public void process(Page page) {
                page.setSkip(true);
            }

            @Override
            public Site getSite() {
                return SiteBuilder.builder().setDomain(UUID.randomUUID().toString()).setSleepTime(0).build();
            }
        }).setDownloader(new Downloader() {
            @Override
            public Page download(Request request, Task task) {
                return new Page().setRawText("");
            }

            @Override
            public void setThread(int threadNum) {

            }
        }).setScheduler(new Scheduler() {

            private AtomicInteger count = new AtomicInteger();

            private Random random = new Random();

            @Override
            public void push(Request request, Task task) {

            }

            @Override
            public synchronized Request poll(Task task) {
                if (count.incrementAndGet() > 1000) {
                    return null;
                }
                if (random.nextInt(100)>90){
                    return null;
                }
                return new Request("test");
            }
        }).thread(10);
        spider.run();
    }
    
    class TestSpider implements Spider {
		private Site site = SiteBuilder.builder().setDomain(UUID.randomUUID().toString()).build();
		
		@Override
		public void process(Page page) {
			// nothing
		}
		
		@Override
		public Site getSite() {
			return site;
		}
	}
}
