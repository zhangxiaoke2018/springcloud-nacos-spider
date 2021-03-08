package com.jinguduo.spider.spider.weibo;

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
import com.jinguduo.spider.data.table.WeiboTagLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class WeiboTagSpiderTests {
    
    @Autowired
    private WeiboTagSpider weiboTagSpider;

    private final static String WEIBO_TOPIC_URL = "http://huati.weibo.com/k/玩转北京";
    
    private final static String WEIBO_TOPIC_JSON_FILE = "/html/WeiboTopic.html";
    private final static String TOPIC_RAW_TEXT = IoResourceHelper.readResourceContent(WEIBO_TOPIC_JSON_FILE);

    @Test
    public void testContext() {
        Assert.notNull(weiboTagSpider);
    }
    
    @Test
    public void testWeiboTopicProcess() throws Exception {
        Job job = new Job();
        job.setUrl(WEIBO_TOPIC_URL);

        DelayRequest delayRequest = new DelayRequest(job);
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(TOPIC_RAW_TEXT);
        
        weiboTagSpider.process(page);
        
        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        
        List<WeiboTagLog> weiboTagLog = resultItems.get(WeiboTagLog.class.getSimpleName());
        Assert.notNull(weiboTagLog);
        Assert.isTrue(2904 == weiboTagLog.get(0).getFeedCount().intValue());
        Assert.isTrue(4548000 == weiboTagLog.get(0).getReadCount().intValue());
        Assert.isTrue(99 == weiboTagLog.get(0).getFollowCount().intValue());
    }
}
