package com.jinguduo.spider.spider.fengxing;


import java.util.List;

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
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/14 下午4:22
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class FengXingSpiderTests {

    @Autowired
    private FengXingSpider fengXingSpider;

    private final static String MOVIE_RAW_TEXT = IoResourceHelper.readResourceContent("/html/FengXingSpiderTests.html");

    @Test
    public void testContext() {
        Assert.notNull(fengXingSpider);
    }

    @Test
    public void testProcessNetMovie() throws Exception {
        Page page = new Page();
        page.setRequest(new DelayRequest(new Job("http://www.fun.tv/vplay/g-301706.e-1?uc=19&webuc=13&alliance=152055")));
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(MOVIE_RAW_TEXT);

        fengXingSpider.process(page);
        ResultItems resultItems = page.getResultItems();
        List<ShowLog> joblist = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(joblist);
    }
}

