package com.jinguduo.spider.spider.fengxing;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

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

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class FxCommentSpiderTests {
    @Autowired
    private FxCommentSpider fxCommentSpider;

    final static String COMMENT_URL = "http://api1.fun.tv/comment/display/gallery/312359?pg=1&isajax=1&dtime=1496730211431";

    DelayRequest delayRequest;

    @Value("classpath:json/FengxingCommentApiSpider.json")
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
    public void testProcessPc() throws Exception{
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(IoResourceHelper.readResourceContent(resource));

        fxCommentSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLog);
    }
}
