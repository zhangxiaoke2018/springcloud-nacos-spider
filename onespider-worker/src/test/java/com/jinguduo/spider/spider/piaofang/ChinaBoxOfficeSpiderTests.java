package com.jinguduo.spider.spider.piaofang;


import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.BoxOfficeLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

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

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ChinaBoxOfficeSpiderTests {

    @Autowired
    private ChinaBoxOfficeSpider chinaBoxOfficeSpider;

    private String url="http://www.cbooo.cn/m/657862";

    @Value("classpath:html/boxOffice.html")
    private Resource rawText;

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setUrl(url);
        job.setFrequency(100);
        job.setMethod("GET");
        delayRequest = new DelayRequest(job);
    }


    @Test
    public void testContext() {
        Assert.notNull(chinaBoxOfficeSpider);
    }

    @Test
    public void testProcessOk() throws Exception {
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setUrl(new PlainText(url));
        page.setRawText(IoResourceHelper.readResourceContent(rawText));
        chinaBoxOfficeSpider.process(page);
        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<BoxOfficeLogs> boxOffices = resultItems.get(BoxOfficeLogs.class.getSimpleName());
        Assert.isTrue(boxOffices.size() > 0);
    }

}
