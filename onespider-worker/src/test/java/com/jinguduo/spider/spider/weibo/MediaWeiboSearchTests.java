package com.jinguduo.spider.spider.weibo;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.data.table.WeiboText;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class MediaWeiboSearchTests {

    private final static String JSON_FILE = "/html/MediaWeiboSearchNew.html";
    private final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);
    public final static String URL = "https://s.weibo.com/weibo/有翡?topnav=1&wvr=6&b=1";

    @Autowired
    private WeiboSearchSpider weiboSearchSpider;

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() throws Exception {
//        Assert.notNull(mediaWeiboSearchSpider);
//        Assert.notNull(RAW_TEXT);

        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(URL));

        weiboSearchSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        List<WeiboText> weiboTextList = resultItems.get(WeiboText.class.getSimpleName());

        Assert.isTrue(weiboTextList.size() > 0);
    }

    @Test
    public void testHuati(){
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(URL);
        job.setCode("131960");

        DelayRequest delayRequest = new DelayRequest(job,0);
        SpiderEngine.create(weiboSearchSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
    }

}
