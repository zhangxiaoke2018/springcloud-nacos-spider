package com.jinguduo.spider.spider.letv;


import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

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
public class LePlayCountSpiderTests {

    @Autowired
    private LePlayCountSpider lePlayCountSpider;

    final static String VID = "25775257";
    final static String URL = String.format("http://v.stat.letv.com/vplay/queryMmsTotalPCount?vid=%s", VID);

    private final static String JSON_FILE = "/json/LePlayCountSpiderTests.json";
    private final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    final static String COMMENT_COUNT_URL = "http://v.stat.letv.com/vplay/queryMmsTotalPCount?pid=85010&cid=11&vid=26142039";
    DelayRequest delayRequest;
    DelayRequest comment_delayRequest;
    @Before
    public void setup()  {
        Job job = new Job(URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setMethod("GET");
        delayRequest = new DelayRequest(job);

        Job job_com = new Job(COMMENT_COUNT_URL);
        job_com.setPlatformId(1);
        job_com.setShowId(1);
        job_com.setFrequency(100);
        job_com.setMethod("GET");
        comment_delayRequest = new DelayRequest(job_com);
    }

    @Test
    public void testContext() {
        Assert.notNull(lePlayCountSpider);
    }

    @Test
    public void testRun() throws Exception {
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(URL));

        lePlayCountSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLog);

    }
    @Test
    public void testCommentCountRun() throws Exception {
        Page page = new Page();
        page.setRequest(comment_delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(COMMENT_COUNT_URL));

        lePlayCountSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        List<CommentLog> commentLog =  resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLog, "\"testCommentCountRun fail by url : "+COMMENT_COUNT_URL);
        Assert.isTrue(commentLog.get(0).getCommentCount() > 0, "testCommentCountRun fail by url : "+COMMENT_COUNT_URL);

    }


}
