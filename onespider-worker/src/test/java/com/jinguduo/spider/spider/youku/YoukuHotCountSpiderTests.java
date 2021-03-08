package com.jinguduo.spider.spider.youku;

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
public class YoukuHotCountSpiderTests {

    @Autowired
    private YoukuAcsDomainSpider youkuHotCountSpider;

    final static String JSON_FILE = "/json/youkuHotCount.json";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    final static String ALBUM_ID = "422060400";
    final static String URL = "http://www.iqiyi.com/v_19rrkchixo.html#vfrm=2-4-0-1";

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
        Assert.notNull(youkuHotCountSpider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() {

        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);

        youkuHotCountSpider.pageProcess(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        @SuppressWarnings("unchecked")
        List<Job> jobs = (List<Job>) resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs);

        Assert.notNull(jobs.get(0).getShowId());
        Assert.notNull(jobs.get(0).getUrl());

        Assert.isTrue(jobs.get(jobs.size() - 1).getUrl().startsWith("http://data.video.qq.com/fcgi-bin/data?tid=70&&appid=10001007&appkey=e075742beb866145&callback=jQuery1"));

    }

}
