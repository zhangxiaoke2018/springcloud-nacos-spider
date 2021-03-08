package com.jinguduo.spider.spider.sohu;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.table.ToutiaoNewLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SohuMyMediaSpiderTests {

    @Autowired
    private SohuMyMediaSpider sohuMyMediaSpider;

    private static final String url = "http://my.tv.sohu.com/pl/9294859/index.shtml";

    @Value("classpath:html/SohuMyMedia.html")
    private Resource testJson;

    private DelayRequest delayRequest;

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

        sohuMyMediaSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        List<Show> show = resultItems.get(Show.class.getSimpleName());
        Assert.isTrue(show.size()>=0);
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.isTrue(jobs.size()>=0);
    }
}
