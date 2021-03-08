package com.jinguduo.spider.spider.toutiao;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.ToutiaoNewLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by lc on 2017/5/4.
 */

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ToutiaoNewsSpiderTests {
    @Autowired
    private ToutiaoNewsSpider toutiaoSpider;

    private final Logger log = LoggerFactory.getLogger(ToutiaoNewsSpiderTests.class);

    private static final String url = "http://www.toutiao.com/search_content/?offset=0&format=json&keyword=hello！女主播&autoload=true&count=200&cur_tab=1";

    @Value("classpath:json/newsToutiao.json")
    private Resource testJson;


    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(url);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }


    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(IoResourceHelper.readResourceContent(testJson));
        page.setUrl(new PlainText(url));

        toutiaoSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<ToutiaoNewLogs> allToutiao = (List) resultItems.get("ToutiaoNewLogs");
        for (ToutiaoNewLogs tt : allToutiao) {
            System.out.println(tt.getTitle());
        }
        Assert.notNull(allToutiao);
    }

}
