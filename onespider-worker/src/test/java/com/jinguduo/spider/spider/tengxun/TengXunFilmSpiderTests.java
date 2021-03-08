package com.jinguduo.spider.spider.tengxun;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
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
public class TengXunFilmSpiderTests {

    @Autowired
    private TengXunFilmSpider tengXunFilmSpider;

    final static String URL = TengXunFilmSpiderIT.URL;

    DelayRequest delayRequest;

    @Value("classpath:html/TengXunFilmTests.html")
    private Resource resource;

    @Before
    public void setup()  {
        Job job = new Job(URL);
        job.setPlatformId(2);
        job.setShowId(2);
        job.setFrequency(100);
        job.setCode(TengXunFilmSpiderIT.CODE);
        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(tengXunFilmSpider);
    }

    @Test
    public void testProcessPc() throws Exception {
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(IoResourceHelper.readResourceContent(resource));
        page.setUrl(new PlainText(URL));
        tengXunFilmSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs);

    }

}
