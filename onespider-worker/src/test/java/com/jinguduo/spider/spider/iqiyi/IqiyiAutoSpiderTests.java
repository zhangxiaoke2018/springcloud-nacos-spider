package com.jinguduo.spider.spider.iqiyi;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiAutoSpiderTests {

    @Autowired
    private IqiyiAutoSpider iqiyiPageSpider;

    final static String HTML_FILE = "/html/Iqiyi_auto.html";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(HTML_FILE);

    final static String ALBUM_ID = "552945600";
    final static String URL = "http://list.iqiyi.com/www/1/------27401-------4-1-1-iqiyi--.html";
    final static String NAME = "特殊生物调查科";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job(URL);
        job.setCode(ALBUM_ID);

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(iqiyiPageSpider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);

        iqiyiPageSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        @SuppressWarnings("unchecked")
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);

        Assert.notNull(jobs.get(0).getCode());
        Assert.notNull(jobs.get(0).getUrl());

        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        Assert.notNull(shows);

    }
}
