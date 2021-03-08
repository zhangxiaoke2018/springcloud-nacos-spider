package com.jinguduo.spider.spider.weibo;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;

import com.jinguduo.spider.data.table.WeiboOfficialLog;
import com.jinguduo.spider.data.table.WeiboText;
import com.jinguduo.spider.webmagic.ResultItems;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;


/**
 * Created by lc on 2017/5/12.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

@Slf4j
public class MediaWeiboOfficialApiSpiderTest {

    @Autowired
    private WeiboOfficialApiSpider weiboOfficialApiSpider;

    private DelayRequest delayRequest;

    private final String testUrl = "https://weibo.com/xiaozhan1?is_hot=1";
    private final String testTagUrl = "https://weibo.com/p/100808f57b3faeb25f86b43a8cc58f02286f40/super_index#%E8%82%96%E6%88%98";


    @Before
    public void setup()  {

        //loading job
        Job job = new Job(testUrl);
        job.setPlatformId(17);
        job.setShowId(1);
        job.setFrequency(100);

        //simulate request
        delayRequest = new DelayRequest(job);

    }

    @Test
    public void testInvolveCount() {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(testUrl);
        job.setCode("1");
        DelayRequest delayRequest = new DelayRequest(job);

        SpiderEngine.create(weiboOfficialApiSpider)
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline)
                .addRequest(delayRequest)
                .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<WeiboOfficialLog> weiboTextList = resultItems.get(WeiboOfficialLog.class.getSimpleName());

        Assert.isTrue(weiboTextList.size() > 0);
    }

    @Test
    public void testTag() {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(testTagUrl);
        job.setCode("51e11e267593446a5fbd5aa48cce540f");
        DelayRequest delayRequest = new DelayRequest(job);

        SpiderEngine.create(weiboOfficialApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
    }
}
