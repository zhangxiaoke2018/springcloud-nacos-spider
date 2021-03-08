package com.jinguduo.spider.spider.kankan;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.ResultItems;

/** 土豆视频弹幕抓取测试类
 * create by gsw 2016-12-28
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class KanKanBarrageContentSpiderIT {

    @Autowired
    private KankanBarrageContentSpider kankanBarrageSpider;

    @Test
    public void searchBarrageContentTest()  {
        Job newJob = new Job("http://point.api.t.kankan.com/danmu.json?a=show&subid=495713&start=1&end=20&jsobj=danmuobj");
        newJob.setPlatformId(1);
        newJob.setShowId(1);
        newJob.setFrequency(100);
        newJob.setMethod("GET");
        newJob.setCode("9182738");
        DelayRequest delayRequest = new DelayRequest(newJob, JobKind.Once.getValue());
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(kankanBarrageSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageText> danmuLogs = resultItems.get(BarrageText.class.getSimpleName());
        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);
        Assert.isTrue(!CollectionUtils.isEmpty(danmuLogs));
    }
}
