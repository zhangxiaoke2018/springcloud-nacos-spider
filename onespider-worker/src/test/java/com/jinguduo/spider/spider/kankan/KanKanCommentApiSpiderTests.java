package com.jinguduo.spider.spider.kankan;


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
public class KanKanCommentApiSpiderTests {

    @Autowired
    private KanKanCommentApiSpider kanKanCommentApiSpider;

    final static String COMMENT_URL = "http://api.t.kankan.com/weibo_list_vod.json?jsobj=hotscomment&hot=1&movieid=90357";

    DelayRequest delayRequest;

    @Value("classpath:json/KanKanCommentJsonTests.json")
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
    public void testProcessPc() throws Exception {
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(IoResourceHelper.readResourceContent(resource));

        kanKanCommentApiSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<CommentLog> commentLogList = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLogList);
    }

}
