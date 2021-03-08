package com.jinguduo.spider.spider.weibo;

import java.util.List;

import com.jinguduo.spider.data.table.WeiboIndexHourLog;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.ExponentLog;
import com.jinguduo.spider.data.table.WeiboAttribute;
import com.jinguduo.spider.data.table.WeiboProvinceCompare;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.pipeline.ConsolePipeline;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class WeiboChartDatasPageSpiderIT {

    @Autowired
    private WeiboChartDatasPageSpider weiboChartDatasPageSpider;

    final static String EXPONENT_BASE_URL = "http://data.weibo.com/index/ajax/hotword?flag=nolike&word=人民的名义";
    final static String EXPONENT_PROCESS_URL = "http://data.weibo.com/index/ajax/getchartdata?wid=1061704100000146164&wname=%E4%BA%BA%E6%B0%91%E7%9A%84%E5%90%8D%E4%B9%89&month=default&__rnd=1496889629782";

    @Before
    public void setup()  {

    }

    @Test
    public void testContext() {
        Assert.notNull(weiboChartDatasPageSpider, "微博指数Spider 为null!");
    }

    /***
     * 微指数页面入口
     */
    @Test
    public void testNewWid()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://data.weibo.com/index/ajax/newindex/searchword?word=老九门");
        job.setCode("asgsagasgafasfas");

        SpiderEngine.create(weiboChartDatasPageSpider)
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(new DelayRequest(job))
                .run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "微博指数获取【Wid】返回结果为空");
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(CollectionUtils.isNotEmpty(jobs) && jobs.size() == 1, "微博指数获取【Wid】,生成任务测试不通过");
    }


    /***
     * 微指数页面入口
     */
    @Test
    public void testWid()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(EXPONENT_BASE_URL);
        job.setCode("asgsagasgafasfas");

        SpiderEngine.create(weiboChartDatasPageSpider)
                .setDownloader(new DownloaderManager())
                .addSpiderListeners(Lists.newArrayList(new WeiboChartDatasListener()))
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(new DelayRequest(job))
                .run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "微博指数获取【Wid】返回结果为空");
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(CollectionUtils.isNotEmpty(jobs) && jobs.size() == 3, "微博指数获取【Wid】,生成任务测试不通过");
    }

    /***
     * 解析微指数趋势json
     */
    @Test
    public void testTendency()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://data.weibo.com/index/ajax/newindex/getchartdata?wid=1061211200000065997&dateGroup=1month");
        job.setCode("0");

        SpiderEngine.create(weiboChartDatasPageSpider)
                .setDownloader(new DownloaderManager())
//                .addSpiderListeners(Lists.newArrayList(new WeiboChartDatasListener()))
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(new DelayRequest(job))
                .run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "微博指数【热度趋势】返回结果为空");
        List<ExponentLog> list = resultItems.get(ExponentLog.class.getSimpleName());
        list.forEach(l -> System.out.println(l));

    }
    @Test
    public void testTendencyHour()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://data.weibo.com/index/ajax/newindex/getchartdata?wid=1061211200000065997&dateGroup=1day");
        job.setCode("0");

        SpiderEngine.create(weiboChartDatasPageSpider)
                .setDownloader(new DownloaderManager())
//                .addSpiderListeners(Lists.newArrayList(new WeiboChartDatasListener()))
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(new DelayRequest(job))
                .run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "微博指数【热度趋势】返回结果为空");
        List<WeiboIndexHourLog> list = resultItems.get(WeiboIndexHourLog.class.getSimpleName());
        list.forEach(l -> System.out.println(l));

    }

    /**
     * 微博属性分析
     * @
     */
    @Test
    public void testPro()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://data.weibo.com/index/ajax/getattributealldata?data=%7B%22key2%22:%7B%22id%22:%221061704100000146164%22,%22word%22:%22%E4%BA%BA%E6%B0%91%E7%9A%84%E5%90%8D%E4%B9%89%22%7D%7D&item=all&__rnd=" + System.currentTimeMillis());
        job.setCode("1492570232521");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(weiboChartDatasPageSpider)
                .addSpiderListeners(Lists.newArrayList(new WeiboChartDatasListener()))
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline).addPipeline(new ConsolePipeline())
                .addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<WeiboAttribute> weiboAttribute = resultItems.get(WeiboAttribute.class.getSimpleName());
        Assert.isTrue(CollectionUtils.isNotEmpty(weiboAttribute), "微博指数【属性】返回结果为空");
    }

    /**
     * 微博地域解读   http://data.weibo.com/index/ajax/keywordzone?wid=1061309240001932005&wname=徐好&type=default&__rnd=1492764354243&_t=0&__rnd=1492764374186
     */
    @Test
    public void testR()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
//        job.setUrl("http://data.weibo.com/index/ajax/keywordzone?wid=1061704100000146164&wname=徐好&type=default&__rnd=" + System.currentTimeMillis());
        job.setUrl("http://data.weibo.com/index/ajax/keywordzone?type=notdefault&wid=4scka4lHjEIm&wname=血色苍穹&__rnd=" + System.currentTimeMillis());
        job.setCode("1492570232521");
        DelayRequest delayRequest = new DelayRequest(job);

        SpiderEngine.create(weiboChartDatasPageSpider)
                .addSpiderListeners(Lists.newArrayList(new WeiboChartDatasListener()))
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline)
                .addPipeline(new ConsolePipeline())
                .addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems, "微博指数【地域】单元测试返回空结果集!");
        List<WeiboProvinceCompare> weiboProvinceCompare = resultItems.get(WeiboProvinceCompare.class.getSimpleName());
        Assert.isTrue(CollectionUtils.isNotEmpty(weiboProvinceCompare), "微博指数【地域】单元测试返回空结果集!");
    }
}
