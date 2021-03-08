package com.jinguduo.spider.spider.youku;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class YouKuCommentCountSpiderIT {

    @Autowired
    private YouKuCommentCountSpider youKuCommentCountSpider;

    @Test
    public void testContext() {
        Assert.notNull(youKuCommentCountSpider);
    }

    @Test
    public void testCommentCount() {
        Job job = new Job("http://p.comments.youku.com/ycp/comment/pc/commentList?app=100-DDwODVkv&objectId=1142447246&objectType=1&listType=0&currentPage=1&pageSize=1&sign=3df5f4567f169972380e1ee9e070d593&time=1484114120");
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youKuCommentCountSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentLog> cl = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(cl);
        log.debug(cl);
    }


}
