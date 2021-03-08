package com.jinguduo.spider.spider.sohu;


import java.util.List;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SohuCommentSpiderIT {
    @Autowired
    private SohuCommentSpider sohuCommentSpider;

    private DelayRequest delayRequest_vlist;

    /** 幻城剧集api */
    private final static String VIDEO_LIST_URL = "http://changyan.sohu.com/api/2/topic/load?client_id=cyqyBluaj&topic_url=http://tv.sohu.com/20160920/n468816893.shtml&topic_source_id=3241302";

    /**评论文本URL*/
    private final static String COMMENT_URL = "http://changyan.sohu.com/api/2/topic/comments?client_id=cyqyBluaj&page_no=1&page_size=30&topic_id=2526867076";

    @Before
    public void setup()  {

        //loading job
        Job job = new Job(VIDEO_LIST_URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);

        //simulate request
        delayRequest_vlist = new DelayRequest(job);

    }

    @Test
    public void testCommentCount() {

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuCommentSpider).addPipeline(testPipeline).addRequest(delayRequest_vlist).run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.notNull(resultItems);
        List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLog);
    }

    /**
     * 评论文本抓取测试
     */
    @Test
    public void testCommentText()  {
        Job job = new Job(COMMENT_URL);
        job.setPlatformId(1);
        job.setFrequency(FrequencyConstant.COMMENT_COUNT);
        job.setCode("3570471");

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuCommentSpider).addPipeline(testPipeline).addRequest(new DelayRequest(job)).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentText> commentTextList = resultItems.get(CommentText.class.getSimpleName());
        Assert.notNull(commentTextList);

    }

    @After
    public void restResource () {
    }
}
