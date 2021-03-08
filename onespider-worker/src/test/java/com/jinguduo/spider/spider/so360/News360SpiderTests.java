package com.jinguduo.spider.spider.so360;


import java.util.List;

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
import com.jinguduo.spider.data.table.News360Log;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class News360SpiderTests {
    @Autowired
    private News360Spider news360InvolveCountApiSpider;

    final static String JSON_FILE = "/html/News360.html";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(News360SpiderIT.URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(news360InvolveCountApiSpider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(News360SpiderIT.URL));

        news360InvolveCountApiSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<News360Log> news360Log = resultItems.get(News360Log.class.getSimpleName());
        Assert.notNull(news360Log);
        Assert.isTrue(69 == news360Log.get(0).getCount().intValue());
    }
}
