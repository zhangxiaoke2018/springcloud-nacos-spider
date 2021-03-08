package com.jinguduo.spider.spider.mgtv;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.spider.mgtv.MgtvBarrageSpider;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class MgtvBarrageSpiderTests {

    @Autowired
    private MgtvBarrageSpider tengxunBarrageSpider;

    private final static String JSON_FILE = "/json/TengxunBarrageSpider.json";

    private final static String URL = "http://galaxy.person.mgtv.com/rdbarrage?&time=0&vid=4068250&cid=316381";

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setCode("4068250");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() throws Exception {
        final String failMessage = "test tengxunBarrageSpider context process fail!";
        final String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);
        Assert.isTrue(StringUtils.isNotBlank(RAW_TEXT), "none json");

        Page page = new Page();
        page.setUrl(new PlainText(URL));
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);

        tengxunBarrageSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, failMessage);

        List<BarrageText> barrageTexts = resultItems.get(BarrageText.class.getSimpleName());
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(
                CollectionUtils.isNotEmpty(barrageTexts) && barrageTexts.size() == 364,
                failMessage
        );
        Assert.isTrue(
                CollectionUtils.isNotEmpty(jobs) && barrageTexts.get(0).getCode() == delayRequest.getJob().getCode(),
                failMessage
        );
    }
}

