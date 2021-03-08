package com.jinguduo.spider.spider.douban;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.DoubanCommentsText;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by jack on 2017/7/18.
 *
 * 爬取豆瓣M站页面和接口
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DoubanMobileSpiderIT {

    private String themePage1 = "https://m.douban.com/movie/subject/26260853/"; //专题页面，电影
    private String themePage2 = "https://movie.douban.com/subject/27598254/"; //专题页面，电视剧

    private String creditsUrl1 = "https://m.douban.com/rexxar/api/v2/movie/26260853/credits"; //影人接口，电影
    private String creditsUrl2 = "https://m.douban.com/rexxar/api/v2/tv/26859982/credits"; //影人接口，电视剧

    private String commentsUrl1 = "https://m.douban.com/rexxar/api/v2/movie/26260853/interests?count=20&order_by=hot&start=72300&ck=&for_mobile=1"; //评论接口，电影
    private String commentsUrl2 = "https://m.douban.com/rexxar/api/v2/tv/26859982/interests?count=20&order_by=hot&start=0&ck=&for_mobile=1"; //评论接口，电视剧
    @Autowired
    DoubanMobileSpider doubanMobileSpider;

    @Test
    public void testThemePage(){
        TestPipeline pipeline = new TestPipeline();
        Job job = new Job(themePage2);
        job.setCode("27598254");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(doubanMobileSpider).addPipeline(pipeline).addRequest(delayRequest).run();
        ResultItems resultItems = pipeline.getResultItems();
        List doubanCommentsTexts = resultItems.get(DoubanCommentsText.class.getSimpleName());
        Assert.isTrue(doubanCommentsTexts.size()>0);
    }

//    @Test
//    public void testCredits(){
//        TestPipeline pipeline = new TestPipeline();
//        Job job = new Job(creditsUrl1);
//        job.setCode("34567891");
//        DelayRequest delayRequest = new DelayRequest(job);
//        SpiderEngine.create(doubanMobileSpider).addPipeline(pipeline).addRequest(delayRequest).run();
//        ResultItems resultItems = pipeline.getResultItems();
//        List doubanCommentsTexts = resultItems.get(DoubanCommentsText.class.getSimpleName());
//        Assert.isTrue(doubanCommentsTexts.size()>0);
//    }

    @Test
    public void testComments(){
        TestPipeline pipeline = new TestPipeline();
        Job job = new Job(commentsUrl2);
        job.setCode("23456789");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(doubanMobileSpider).addPipeline(pipeline).addRequest(delayRequest).run();
        ResultItems resultItems = pipeline.getResultItems();
        List<DoubanCommentsText> doubanCommentsTexts = resultItems.get(DoubanCommentsText.class.getSimpleName());
        Assert.isTrue(doubanCommentsTexts.size()>0);
    }
}
