package com.jinguduo.spider.spider.iqiyi;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiBarrageSpiderIT {

    @Autowired
    private IqiyiBarrageSpider iqiyiBarrageSpider;



//    private String URL = "http://cmts.iqiyi.com/bullet/08/00/594220800_300_5.z?rn=0.5487082269974053&business=danmu&is_iqiyi=true&is_video_page=true&tvid=594220800";
    private final static String URL = "http://cmts.iqiyi.com/bullet/38/00/727453800_300_1.z?rn=0.5487082269974053&business=danmu&is_iqiyi=true&is_video_page=true&tvid=727453800";
    private final static String URL_404 = "http://cmts.iqiyi.com/bullet/02/00/575580300_300_19.z?rn=0.5487082269974053&business=danmu&is_iqiyi=true&is_video_page=true&tvid=575580300";

    @Before
    public void setup()  {
    }

    @Test
    public void testContent()  {
         // request
        Job job = new Job();
        job.setUrl("http://cmts.iqiyi.com/bullet/39/00/681143900_300_1.z?rn=0.5487082269974053&business=danmu&is_iqiyi=true&is_video_page=true&tvid=681143900");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine
                .create(iqiyiBarrageSpider)
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline)
                .addRequest(delayRequest)
                .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<BarrageText> barrageTextList = resultItems.get(BarrageText.class.getSimpleName());
        Assert.notEmpty(barrageTextList, "test iqiyi barrageTextList fail!");
        Assert.isTrue(barrageTextList.size()>=1, "test iqiyi barrageTextList fail!");
        BarrageText barrageText = barrageTextList.get(0);
        Assert.notNull(barrageText, "test iqiyi barrage fail");
    }
    
    @Test
    public void testStatus404()  {
         // request
        Job job = new Job();
        job.setUrl(URL_404);
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine
                .create(iqiyiBarrageSpider)
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline)
                .addRequest(delayRequest)
                .run();

        ResultItems resultItems = testPipeline.getResultItems();
        Assert.isNull(resultItems, "Is Ok");
    }
}
