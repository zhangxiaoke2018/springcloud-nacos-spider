package com.jinguduo.spider.spider.dongmanmanhua;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicDmmh;
import com.jinguduo.spider.webmagic.ResultItems;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DongManComicApiSpiderIT {

    @Autowired
    private DongManComicApiSpider dongManComicApiSpider;

    @Test
    public void testReply(){
        // 链接需要签名，会过期
        Job job = new Job("https://apis.dongmanmanhua.cn/app/title/info2?titleNo=1328&v=2&platform=APP_ANDROID&serviceZone=CHINA&language=zh-hans&md5=GfcGH11YKM7NlwtlzWw1nw&expires=1555055770");
        job.setPlatformId(52);
        job.setShowId(1);
        job.setCode("12345678");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(dongManComicApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());
        List<ComicDmmh> comicDmmh = resultItems.get(ComicDmmh.class.getSimpleName());
    }

    @Test
    public void testEpisode(){
        // 链接需要签名，会过期
        Job job = new Job("http://apis.dongmanmanhua.cn/app/episode/list/v3?titleNo=423&startIndex=0&pageSize=50000&serviceZone=CHINA&v=7&platform=APP_ANDROID&language=zh-hans&md5=iepwWEEvPaWjoCg8apRA-Q&expires=1555062227");
        job.setPlatformId(52);
        job.setShowId(1);
        job.setCode("12345678");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(dongManComicApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());
        List<ComicDmmh> comicDmmh = resultItems.get(ComicDmmh.class.getSimpleName());
    }
}
