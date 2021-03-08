package com.jinguduo.spider.spider.letv;


import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class LePageSpiderTests {

    @Autowired
    private LePageSpider leTvPageSpider;

    private final static String URL = "http://www.le.com/zongyi/85010.html";

    private final static String MOVIE_URL = "http://www.le.com/ptv/vplay/24923900.html?ch=360_ffdy";

    private final static String RAW_TEXT = IoResourceHelper.readResourceContent("/html/LeSpiderTests.html");

    private final static String MOVIE_RAW_TEXT = IoResourceHelper.readResourceContent("/html/LePageSpiderMovieTests.html");

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(leTvPageSpider);
    }


    @Test
    public void testRun() throws Exception {

        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);

        leTvPageSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        Assert.isTrue(jobs.size() == 2);
    }

    @Test
    public void testProcessNetMovie() throws Exception {
        Page page = new Page();
        page.setRequest(new DelayRequest(new Job(MOVIE_URL)));
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(MOVIE_RAW_TEXT);

        leTvPageSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs);
    }


}
