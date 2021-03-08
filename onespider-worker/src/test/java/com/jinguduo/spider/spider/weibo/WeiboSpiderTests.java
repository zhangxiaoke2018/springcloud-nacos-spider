package com.jinguduo.spider.spider.weibo;


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

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.WeiboOfficialLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class WeiboSpiderTests {

    @Autowired
    private WeiboOfficialApiSpider weiboOfficialApiSpider;

    final static String JSON_FILE = "/html/WeiboOfficial.html";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);
    final static String URL = "http://weibo.com/yingshibaidashuo?refer_flag=1001030101_&is_hot=1";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(weiboOfficialApiSpider);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(URL));

        weiboOfficialApiSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<WeiboOfficialLog> weiboOfficialLog = resultItems.get(WeiboOfficialLog.class.getSimpleName());
        Assert.notNull(weiboOfficialLog);
        Assert.isTrue(737289 == weiboOfficialLog.get(0).getFansCount().intValue());
        Assert.isTrue(68 == weiboOfficialLog.get(0).getFollowCount().intValue());
        Assert.isTrue(645 == weiboOfficialLog.get(0).getPostCount().intValue());
    }
    
}
