package com.jinguduo.spider.spider.douban;

import java.io.FileNotFoundException;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import com.jinguduo.spider.data.table.DoubanLog;
import com.jinguduo.spider.service.QiniuService;
import com.jinguduo.spider.spider.douban.DoubanMovieSpider;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DoubanMovieSpiderTests {
    @Autowired
    @InjectMocks
    private DoubanMovieSpider doubanMovieSpider;

    final static String JSON_FILE = "/html/DoubanOfficial.html";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    DelayRequest delayRequest;

    @Mock
    private QiniuService qiniuService;

    @Before
    public void setup() throws FileNotFoundException {

        MockitoAnnotations.initMocks(this);
        Mockito.when(qiniuService.upload(Mockito.anyString())).thenReturn("http://www.xxxxxx.com");

        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(DoubanMovieSpiderIT.DOUBAN_MOVIE_URL_1);
        job.setFrequency(100);
        job.setCode("26728159");
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(doubanMovieSpider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(DoubanMovieSpiderIT.DOUBAN_MOVIE_URL_1));

        doubanMovieSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<DoubanLog> doubanLogs = resultItems.get(DoubanLog.class.getSimpleName());
        Assert.notNull(doubanLogs);
        Assert.isTrue(doubanLogs.get(0).getBriefComment() > 0);
        Assert.isTrue(doubanLogs.get(0).getAllPdBriefComment() > 0);
        Assert.isTrue(doubanLogs.get(0).getDiscussionCount() > 0);
        Assert.isTrue(doubanLogs.get(0).getJudgerCount() > 0);
        Assert.isTrue(doubanLogs.get(0).getReviewCount() > 0);
    }
}