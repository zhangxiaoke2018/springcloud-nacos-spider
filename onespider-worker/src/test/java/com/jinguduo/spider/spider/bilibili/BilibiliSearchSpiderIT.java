package com.jinguduo.spider.spider.bilibili;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

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
import com.jinguduo.spider.data.table.BilibiliVideoClick;
import com.jinguduo.spider.data.table.BilibiliVideoDm;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 2016/10/25 下午2:43
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
public class BilibiliSearchSpiderIT {

    @Autowired
    private BiliBiliSearchSpider biliSearchSpider;

    @Test
    public void testContext() {
        Assert.notNull(biliSearchSpider);
    }

    @Test
    public void testTotalrank() {

        Job j = new Job("http://search.bilibili.com/all?keyword=亚人&page=1&order=totalrank");
        j.setPlatformId(1);
        j.setShowId(1);
        DelayRequest firstDelayRequest = new DelayRequest(j);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliSearchSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
    }

    @Test
    public void testClick() {

        Job j = new Job("http://search.bilibili.com/all?keyword=亚人&page=1&order=click");
        j.setPlatformId(1);
        j.setShowId(1);
        DelayRequest firstDelayRequest = new DelayRequest(j);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliSearchSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BilibiliVideoClick> bilibiliVideoClicks =  resultItems.get(BilibiliVideoClick.class.getSimpleName());
        Assert.isTrue(bilibiliVideoClicks.size() > 0, "不小于一个");
    }

    @Test
    public void testDm() {
        Job job = new Job("https://search.bilibili.com/all?keyword=亚人&order=dm");
        job.setPlatformId(1);
        job.setShowId(1);
        DelayRequest request = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(biliSearchSpider).addPipeline(testPipeline).addRequest(request).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BilibiliVideoDm> res = resultItems.get(BilibiliVideoDm.class.getSimpleName());
        Assert.notNull(resultItems, "bilibili search dm resultItems null 了");
        Assert.isTrue(res.size()>0,"结果集不能为空");
    }

}
