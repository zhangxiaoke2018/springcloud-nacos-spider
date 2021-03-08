package com.jinguduo.spider.spider.tengxun;


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

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunUrlSpiderTests {

    @Autowired
    private TengxunUrlSpider tengxunUrlSpider;

    final static String JSON_FILE = "/html/TengxunUrlSpiderTests.html";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    final static String URL = "http://v.qq.com/x/cover/aqg908heolora7c.html";
    final static String FIND_SEED_URL = "https://v.qq.com/x/cover/fzfi0p4etjrckhh.html";


    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");
        job.setCode("aqg908heolora7c");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(tengxunUrlSpider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(URL));

        tengxunUrlSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(!jobList.isEmpty());
    }
}
