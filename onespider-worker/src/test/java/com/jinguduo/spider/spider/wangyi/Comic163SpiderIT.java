package com.jinguduo.spider.spider.wangyi;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.Comic163;
import com.jinguduo.spider.webmagic.ResultItems;

import lombok.extern.apachecommons.CommonsLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 31/07/2017 11:39
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class Comic163SpiderIT {

    @Autowired
    private Comic163Spider comicSpider;

    @Autowired
    private Comic163BilibiliSpider comic163BilibiliSpider;

    @Test
    public void testContext() {
        Assert.notNull(comicSpider,"is not null");
    }

    @Test
    public void testSa()  {

        String url = "https://manhua.163.com";

        Job j = new Job(url);
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(comicSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        for (Job job : jobs) {
            log.debug(job);
        }

    }

    @Test
    public void testPager()  {

        String url = "https://manhua.163.com/category/getData.json?sort=2&pageSize=72&page=1";

        Job j = new Job(url);
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(comicSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Comic163> comic163s = resultItems.get(Comic163.class.getSimpleName());
        Assert.isTrue(comic163s.size() == 72);
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());
        Assert.isTrue(comics.size() == 72);
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        Assert.isTrue(jobs.size() == 73);
        for (Job job : jobs) {
            log.debug(job);
        }

    }
@Test
    public void testSource()  {

        String url = "https://163.bilibili.com/source/5108295818580343008";

        Job j = new Job(url);
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(comic163BilibiliSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());
        Assert.isTrue(comics.size() == 1);
        System.out.println(comics);

    }


}
