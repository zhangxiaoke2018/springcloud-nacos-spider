package com.jinguduo.spider.spider.kanmanhua;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicKuaiKan;
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
public class kanmanhuaCommentSpiderIT {

    @Autowired
    private KanmanhuaCommentSpider  kanmanhuaCommentSpider;


    private static final String  URL2= "http://community-hots.321mh.com/comment/hotlist?appId=1&page=%s&pagesize=20&ssid=25934&contentType=2&ssidType=0";

    private static final String  URL3= "http://community-hots.321mh.com/comment/hotlist?appId=1&page=%s&pagesize=20&ssid=106547&contentType=0&ssidType=0";

    private static final String  URL4= "http://community-hots.321mh.com/comment/newgets/?appId=1&page=%s&pagesize=20&ssid=106547&ssidType=0&sorttype=1&commentType=0&FatherId=0&isWater=0";


    @Test
    public void testDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://community-hots.321mh.com/comment/hotlist?appId=1&page=1&pagesize=20&ssid=25934&contentType=0&ssidType=0");
        job.setCode("kan-25934");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(kanmanhuaCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ComicKuaiKan> comicKuaiKans = resultItems.get(ComicKuaiKan.class.getSimpleName());
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());

        Assert.isTrue(comicKuaiKans.size() > 0);
        Assert.isTrue(comics.size() > 0);
    }






}

