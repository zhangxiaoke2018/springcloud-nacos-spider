package com.jinguduo.spider.spider.toutiao;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.ToutiaoNewLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lc on 2017/5/4.
 */

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ToutiaoNewsSpiderIT {
    @Autowired
    private ToutiaoNewsSpider toutiaoSpider;

    private final Logger log = LoggerFactory.getLogger(ToutiaoNewsSpiderIT.class);

    private static final String url = "https://www.toutiao.com/api/search/content/?aid=24&offset=0&format=json&keyword=%E4%BA%B2%E7%88%B1%E7%9A%84%E4%B9%89%E7%A5%81%E5%90%9B&autoload=true&count=20&cur_tab=1&app_name=web_search";

    private static final String url2 = "https://www.toutiao.com/pgc/column/article_list/?column_no=6507463263532354573&media_id=1583395523989517&format=json";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(url);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        TestPipeline testPipeline = new TestPipeline();
        List<SpiderListener> list = new ArrayList();
        list.add(new ToutiaoDownloaderListener());
        SpiderEngine.create(toutiaoSpider)
                .setDownloader(new DownloaderManager())
                .addSpiderListeners(list)
                .addPipeline(testPipeline)
                .addRequest(delayRequest)
                .run();

        ResultItems resultItems = testPipeline.getResultItems();
        System.out.println("----------");
    }

    @Test
    public void testNull() {
        Assert.notNull(toutiaoSpider);
        Assert.notNull(url);
    }

}
