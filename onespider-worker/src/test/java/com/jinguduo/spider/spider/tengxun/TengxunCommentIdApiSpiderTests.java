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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunCommentIdApiSpiderTests {

    @Autowired
    private TengxunCommentIdApiSpider tengxunCommentIdApiSpider;

    final static String JSON_FILE = "/json/TengxunCommentIdApiSpiderTests.json";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    final static String URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=3&vid=j0020qc692d";

    DelayRequest delayRequest;

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
    public void testContext() {
        Assert.notNull(tengxunCommentIdApiSpider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);

        tengxunCommentIdApiSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<Job> job = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(job);

    }

}
