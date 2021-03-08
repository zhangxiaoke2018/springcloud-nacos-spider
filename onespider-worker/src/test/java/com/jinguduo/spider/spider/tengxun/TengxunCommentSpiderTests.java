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
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunCommentSpiderTests {

    @Autowired
    private TengxuntvCommentSpider tengxuntvCommentSpider;

    final static String JSON_FILE = "/json/TengxunTvCommentCountApiSpiderTests.json";
    private static String TENGXUN_COMMENT_COUNT = null;

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl("http://coral.qq.com/article/1477658167/commentnum");
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() throws Exception {
        Assert.notNull(tengxuntvCommentSpider);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        if( null == TENGXUN_COMMENT_COUNT ){
            TENGXUN_COMMENT_COUNT = IoResourceHelper.readResourceContent(JSON_FILE);
        }
        page.setRawText(TENGXUN_COMMENT_COUNT);

        tengxuntvCommentSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLog);

    }

}
