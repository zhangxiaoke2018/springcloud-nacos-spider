package com.jinguduo.spider.spider.so360;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.News360Log;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.After;
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

public class News360SpiderIT {
    @Autowired
    private News360Spider news360InvolveCountApiSpider;

    private DelayRequest delayRequest;

    /** 老九门新闻 */
//    final static String URL = "http://news.baidu.com/ns?ct=1&rn=20&ie=utf-8&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=newstitle&word=%E7%BA%A2%E8%89%B2%E8%AE%B0%E5%BF%86";
  //  final static String URL = "http://news.so.com/ns?j=0&rank=pdate&src=srp&tn=newstitle&scq=&q=%CF%80&pn=1";
    //final static String URL = "http://news.so.com/ns?j=0&rank=pdate&src=srp&tn=newstitle&scq=&q=%E5%A4%A7%E7%89%8C%E5%AF%B9%E7%8E%8B%E7%89%8C&pn=1";
    final static String URL = "http://news.so.com/ns?j=0&rank=pdate&src=srp&tn=newstitle&scq=&q=%E4%BA%86%E4%B8%8D%E8%B5%B7%E7%9A%84%E5%92%94%E5%9A%93&pn=1";
    @Before
    public void setup()  {

        //loading job
        Job job = new Job(URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);

        //simulate request
        delayRequest = new DelayRequest(job);

    }

    @Test
    public void testInvolveCount() {

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(news360InvolveCountApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems);
        List<News360Log> news360Log = resultItems.get(News360Log.class.getSimpleName());
        Assert.notNull(news360Log);
        Assert.isTrue( -1 != news360Log.get(0).getCount() && news360Log.get(0).getCount()>=0);
    }

    @After
    public void restResource () {
    }
}
