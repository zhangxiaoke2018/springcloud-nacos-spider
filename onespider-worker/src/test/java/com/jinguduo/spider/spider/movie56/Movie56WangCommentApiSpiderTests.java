package com.jinguduo.spider.spider.movie56;


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
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class Movie56WangCommentApiSpiderTests {

    @Autowired
    private Movie56WangCommentApiSpider movie56WangCommentApiSpider;

    final static String COMMENT_URL = "http://comment.56.com/trickle/api/commentApi.php?a=flvLatest&vid=MTM3ODg4Mzgy&pct=1&page=1&limit=1";

    DelayRequest delayRequest;

    @Value("classpath:json/Movie56WangComment.json")
    private Resource resource;

    @Before
    public void setup()  {
        Job job = new Job(COMMENT_URL);
        job.setPlatformId(2);
        job.setShowId(2);
        job.setFrequency(100);

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(movie56WangCommentApiSpider);
    }

    @Test
    public void testProcessPc() throws Exception {
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(IoResourceHelper.readResourceContent(resource));

        movie56WangCommentApiSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);



        List<CommentLog> commentLog =  resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLog);
        Assert.isTrue(2 == commentLog.get(0).getCommentCount());
    }
}
