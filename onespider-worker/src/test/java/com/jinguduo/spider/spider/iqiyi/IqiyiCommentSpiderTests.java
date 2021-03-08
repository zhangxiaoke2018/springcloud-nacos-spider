package com.jinguduo.spider.spider.iqiyi;


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

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiCommentSpiderTests {

    @Autowired
    private IqiyiCommentSpider iqiyiCommentSpider;

    final static String TV_ID = "513886900";

    final static String COMMENT_URL = "http://api.t.iqiyi.com/qx_api/comment/get_video_comments?need_total=1&sort=hot&page=1&page_size=1&t=0.055826229439616126&tvid=513886900";

    DelayRequest delayRequest;

    @Value("classpath:json/IqiyiComment.json")
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

        iqiyiCommentSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<CommentLog> commentLogList =  resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLogList);

    }

}
