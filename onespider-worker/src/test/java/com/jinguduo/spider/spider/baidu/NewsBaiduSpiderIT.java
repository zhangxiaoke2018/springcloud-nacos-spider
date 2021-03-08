package com.jinguduo.spider.spider.baidu;


import java.util.List;

import com.jinguduo.spider.data.table.NewsArticleMessageLogs;
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
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.data.table.BaiduNewsLog;
import com.jinguduo.spider.data.table.NewsArticleLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class NewsBaiduSpiderIT {

    @Autowired
    private NewsBaiduSpider newsBaiduSpider;


    @Autowired
    private DetailNewsBaiduSpider detailNewsBaiduSpider;


    @Test
    public void testWebBaiduNews(){
        String url ="https://www.baidu.com/s?rtt=1&bsst=1&cl=2&tn=news&rsv_dl=ns_pc&word=%E7%BD%97%E6%B0%B8%E6%B5%A9&x_bfe_rqs=03E80&x_bfe_tjscore=0.592932&tngroupname=organic_news&newVideo=12&pn=0";
        Job job = new Job(url);
        job.setCode(FetchCodeEnum.getCode(url));

        //simulate request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(newsBaiduSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
        Assert.isTrue(jobs.get(0).getUrl().startsWith("http"),  "Bad");
    }

    /***
     * 百度新闻提及数
     */
    @Test
    public void testBaiduNewsInvolveCount() {
        String url = "http://news.baidu.com/ns?ct=1&rn=20&ie=utf-8&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=newstitle&word=%E7%8B%90%E7%8B%B8%E7%9A%84%E5%A4%8F%E5%A4%A9";
        Job job = new Job(url);
        job.setCode(FetchCodeEnum.getCode(url));
        
        //simulate request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(newsBaiduSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<BaiduNewsLog> baiduNewsLog = resultItems.get(BaiduNewsLog.class.getSimpleName());
        Assert.notEmpty(baiduNewsLog, "method testBaiduNewsInvolveCount fail !!by baiduNewsLog: " +baiduNewsLog);
        Assert.isTrue( -1 != baiduNewsLog.get(0).getCount() && baiduNewsLog.get(0).getCount()>=0, "Bad");
        List<NewsArticleLog> articles = resultItems.get(NewsArticleLog.class.getSimpleName());
        Assert.notEmpty(articles, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
        Assert.isTrue(jobs.get(0).getUrl().startsWith("http"),  "Bad");
    }
    
    @Test
    public void testBaiduNews2() {
        String url = "http://news.baidu.com/ns?ct=1&rn=20&ie=utf-8&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=newstitle&word=%E6%9F%B3%E4%BC%A0%E5%BF%97";
        Job job = new Job(url);
        job.setCode(FetchCodeEnum.getCode(url));
        
        //simulate request
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(newsBaiduSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<BaiduNewsLog> baiduNewsLog = resultItems.get(BaiduNewsLog.class.getSimpleName());
        Assert.notEmpty(baiduNewsLog, "method testBaiduNewsInvolveCount fail !!by baiduNewsLog: " +baiduNewsLog);
        Assert.isTrue( -1 != baiduNewsLog.get(0).getCount() && baiduNewsLog.get(0).getCount()>=0, "Bad");
        List<NewsArticleLog> articles = resultItems.get(NewsArticleLog.class.getSimpleName());
        Assert.notEmpty(articles, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
        Assert.isTrue(jobs.get(0).getUrl().startsWith("http"),  "Bad");
    }
    @Test
    public void testBaiduNews3(){
        String url="https://www.baidu.com/s?ie=utf-8&medium=0&rtt=1&bsst=1&rsv_dl=news_t_sk&cl=2&wd=%E7%BD%97%E6%B0%B8%E6%B5%A9&tn=news&rsv_bp=1&rsv_sug3=1&rsv_sug1=1&rsv_sug7=100&rsv_sug2=0&oq=&rsv_btype=t&f=8&inputT=4&rsv_sug4=901&rsv_sug=1";
        Job job=new Job(url);
        job.setCode(FetchCodeEnum.getCode(url));
        DelayRequest delayRequest = new DelayRequest(job);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(detailNewsBaiduSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<BaiduNewsLog> baiduNewsLog = resultItems.get(BaiduNewsLog.class.getSimpleName());
        Assert.notEmpty(baiduNewsLog, "method testBaiduNewsInvolveCount fail !!by baiduNewsLog: " +baiduNewsLog);
        Assert.isTrue( -1 != baiduNewsLog.get(0).getCount() && baiduNewsLog.get(0).getCount()>=0, "Bad");
        List<NewsArticleMessageLogs> articleMessageLogs = resultItems.get(NewsArticleMessageLogs.class.getSimpleName());
        Assert.notEmpty(articleMessageLogs, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
        Assert.isTrue(jobs.get(0).getUrl().startsWith("http"),  "Bad");
    }
}
