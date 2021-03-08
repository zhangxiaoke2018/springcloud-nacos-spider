package com.jinguduo.spider.spider.baidu;


import java.util.List;

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
import com.jinguduo.spider.data.table.TiebaLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TiebaBaiduSpiderTests {
    @Autowired
    private TiebaBaiduSpider tiebaBaiduSpider;


    @Test
    public void testProcessOk() throws Exception {
        final String JSON_FILE = "/html/TiebaOfficial.html";
        final String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);
        Job job = new Job();
        job.setUrl("https://tieba.baidu.com/f?ie=utf-8&kw=人民的名义&fr=search");
        job.setCode("XXXXXX");
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(job.getUrl()));

        tiebaBaiduSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        List<TiebaLog> tiebaLog = resultItems.get(TiebaLog.class.getSimpleName());
        Assert.notEmpty(tiebaLog, "Bad");
        Assert.isTrue(119439 == tiebaLog.get(0).getFollowCount().intValue(), "Bad");
        Assert.isTrue(180778 == tiebaLog.get(0).getPostCount().intValue(), "Bad");
    }
}
