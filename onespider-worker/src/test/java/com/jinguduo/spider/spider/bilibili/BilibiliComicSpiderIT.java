package com.jinguduo.spider.spider.bilibili;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.spider.weibo.WeiboChartDatasListener;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by lc on 2019/11/20
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class BilibiliComicSpiderIT {

    @Autowired
    private ComicBilibiliSpider spider;


    private String url = "https://manga.bilibili.com/twirp/comic.v1.Comic/GetEntranceForRank?comic_id=27244&type=2";

    private String beginUrl = "https://manga.bilibili.com";

    @Test
    public void test1() {
        Job j = new Job(beginUrl);
        j.setMethod("POST");
        j.setCode("tttt");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addSpiderListeners(Lists.newArrayList(new ComicBillibiliDownloaderListener())).

                addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());

//        Job j2 = jobs.get(0);
//        firstDelayRequest = new DelayRequest(j2);
//        testPipeline = new TestPipeline();
//        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();
//        resultItems = testPipeline.getResultItems();
//        jobs = resultItems.get(Job.class.getSimpleName());
//        List<Comic> comics  = resultItems.get(Comic.class.getSimpleName());
//
//
//        Job j3 = jobs.get(0);
//        firstDelayRequest = new DelayRequest(j3);
//        testPipeline = new TestPipeline();
//        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();
//        resultItems = testPipeline.getResultItems();



        Assert.notEmpty(jobs);
    }

    @Test
    public void test2() {
        Job j = new Job("https://manga.bilibili.com/twirp/comic.v2.Comic/ComicDetail?device=pc&platform=web");
        j.setHttpRequestBody(HttpRequestBody.json("{\"comic_id\":25717 }","utf-8"));
        j.setCode("123481");
        j.setMethod("POST");
        j.setPlatformId(24);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());




        Assert.notEmpty(jobs);
    }

}
