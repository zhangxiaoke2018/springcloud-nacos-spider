package com.jinguduo.spider.spider.tengxun;



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/14 下午3:22
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunPageSpiderTests {

    @Autowired
    private TengxunPageSpider tengxunPageSplider;

    final static String JSON_FILE = "/json/TengxunPageSpliderTests.json";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    final static String ALBUM_ID = "422060400";
    final static String URL = "http://www.iqiyi.com/v_19rrkchixo.html#vfrm=2-4-0-1";

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
        Assert.notNull(tengxunPageSplider);
        Assert.notNull(RAW_TEXT);
    }

    @Test
    public void testProcessOk() {
        // page
        /*Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);

        tengxunPageSplider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        @SuppressWarnings("unchecked")
		List<Job> jobs = (List<Job>) resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs);

        Assert.notNull(jobs.get(0).getShowId());
        Assert.notNull(jobs.get(0).getUrl());

        Assert.isTrue(jobs.get(jobs.size() - 1).getUrl().startsWith("http://data.video.qq.com/fcgi-bin/data?tid=70&&appid=10001007&appkey=e075742beb866145&callback=jQuery1"));
*/
    }
}
