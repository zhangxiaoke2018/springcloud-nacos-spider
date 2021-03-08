package com.jinguduo.spider.spider.sohu;


import java.util.List;

import org.junit.After;
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
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SohuDramaSpiderTests {

    @Autowired
    private SohuDramaSpider sohuDramaSpider;

    private DelayRequest delayRequest_vlist;

    /** 幻城剧集api */
    private static String VIDEO_LIST_URL = "http://pl.hd.sohu.com/videolist?playlistid=9115552&order=0&cnt=1";

    private static String RAW_TEXT = null;

    @Before
    public void setup()  {

        //loading job
        Job job = new Job(VIDEO_LIST_URL);
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);

        //simulate request
        delayRequest_vlist = new DelayRequest(job);

    }

    @Test
    public void testPlaylist() throws Exception {
        Page page = new Page();
        page.setRequest(delayRequest_vlist);
        page.setStatusCode(HttpStatus.OK.value());
        if(null == RAW_TEXT){
            RAW_TEXT = IoResourceHelper.readResourceContent("/json/SohuDramaSpiderTests.json");
        }
        page.setRawText(RAW_TEXT);

        sohuDramaSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        Assert.notNull(resultItems.get(Show.class.getSimpleName()));
        List<Job> jobList = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobList);
    }

    @After
    public void restResource () {
    }

}
