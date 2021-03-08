package com.jinguduo.spider.spider.iqiyi;


import java.util.List;

import com.jinguduo.spider.data.table.ShowLog;
import lombok.extern.apachecommons.CommonsLog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年4月14日 下午3:01:54
 *
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class IqiyiPaoPaoCommentSpiderIT {

    @Autowired
    private IqiyiPaoPaoCommentSpider iqiyiPaoPaoCommentSpider;

    @Before
    public void setup()  {

    }

    @Test
    public void testContext() {
        Assert.notNull(iqiyiPaoPaoCommentSpider);
    }

    @Test
    public void testCommentCountCapture()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://paopao.iqiyi.com/apis/e/starwall/basic_wall.action?authcookie=&device_id=pc_web&agenttype=118&wallId=209705347&atoken=8ffffbc44F3tKBShRo5tC9Bm1J5k01EeIqm1jnhIKXLdVMRB2m11Ctom4");
        job.setCode("123456");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(iqiyiPaoPaoCommentSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notNull(commentLog);
        log.debug(commentLog);
    }

}
