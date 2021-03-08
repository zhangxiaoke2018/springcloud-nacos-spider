package com.jinguduo.spider.spider.weibo;


import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
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
import com.jinguduo.spider.data.table.ExponentLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class WeiboChartDatasPageSpiderTests {

    @Autowired
    private WeiboChartDatasPageSpider weiboChartDatasPageSpider;

    final static String JSON_FILE = "/json/WeiboExponent.json";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(WeiboChartDatasPageSpiderIT.EXPONENT_PROCESS_URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(weiboChartDatasPageSpider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testAnalysisChartProcess() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(WeiboChartDatasPageSpiderIT.EXPONENT_PROCESS_URL));

        weiboChartDatasPageSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<ExponentLog> list = resultItems.get(ExponentLog.class.getSimpleName());
        Assert.isTrue(CollectionUtils.isNotEmpty(list));
    }
}
