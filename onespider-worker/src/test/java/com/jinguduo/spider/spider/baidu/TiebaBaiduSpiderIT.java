package com.jinguduo.spider.spider.baidu;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.TiebaArticleLogs;
import com.jinguduo.spider.data.table.TiebaLog;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Before;
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

public class TiebaBaiduSpiderIT {
    @Autowired
    private TiebaBaiduSpider tiebaOfficialApiSpider;


    @Test
    public void testTiebaOfficialApi()  {
        final String TIEBA_URL = "https://tieba.baidu.com/f?ie=utf-8&kw=赖冠霖&fr=search";
        Job job = new Job();
        job.setUrl(TIEBA_URL);
        job.setCode("");//tieba fid（forum_id）
        DelayRequest delayRequest = new DelayRequest(job);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(tiebaOfficialApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<TiebaLog> tiebaLogs =  resultItems.get(TiebaLog.class.getSimpleName());
        Assert.notEmpty(tiebaLogs, "Bad");

        Request request = resultItems.getRequest();
        Assert.isTrue(TIEBA_URL.equals(request.getUrl()), "Bad");
    }

    @Test
    public void testContent()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job("https://tieba.baidu.com/f?ie=utf-8&kw=超神学院&pn=50");
        SpiderEngine.create(tiebaOfficialApiSpider).addPipeline(testPipeline).addRequest(new DelayRequest(job)).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<TiebaArticleLogs> tas = resultItems.get(TiebaArticleLogs.class.getSimpleName());
        Assert.notEmpty(tas, "Bad");
        Job j = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(j, "Bad");
    }
}
