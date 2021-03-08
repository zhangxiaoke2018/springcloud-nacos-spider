package com.jinguduo.spider.spider.tengxun;

import java.util.List;

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
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * Created by csonezp on 2016/11/24.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunDanmuIdApiSpiderIT {
    @Autowired
    TengxunDanmuIdApiSpider spider;
    
    @Test
    public void getTargetIdTest() {
        Job job = new Job( "http://bullet.video.qq.com/fcgi-bin/target/regist?vid=e0031r8s7u9&cid=xbd1y6fvwl3maoz");
        job.setCode("xbd1y6fvwl3maoz");
        
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider)
            .addPipeline(testPipeline)
            .addRequest(delayRequest)
            .run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void getTargetIdTest2() {
        Job job = new Job("http://bullet.video.qq.com/fcgi-bin/target/regist?vid=p0021r364vu&cid=yw0jk0syhiaeavc");
        job.setCode("_url_2");
        
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider)
            .setDownloader(new DownloaderManager())
            .addPipeline(testPipeline)
            .addRequest(delayRequest)
            .run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
}
