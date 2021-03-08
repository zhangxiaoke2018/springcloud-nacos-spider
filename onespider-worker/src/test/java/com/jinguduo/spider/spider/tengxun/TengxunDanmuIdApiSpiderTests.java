package com.jinguduo.spider.spider.tengxun;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

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

/**
 * Created by csonezp on 2016/11/24.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunDanmuIdApiSpiderTests {

    final static String FILE = "/html/TengXunDanMuID.xml";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(FILE);

    final static String URL = "http://bullet.video.qq.com/fcgi-bin/target/regist?vid=y00221a60w7&cid=rz4mhb6494f12co";

    @Autowired
    TengxunDanmuIdApiSpider spider;

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(spider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);

        spider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        Assert.isTrue(jobs.size() == 2);
    }
}
