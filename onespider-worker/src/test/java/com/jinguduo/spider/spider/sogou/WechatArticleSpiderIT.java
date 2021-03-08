package com.jinguduo.spider.spider.sogou;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class WechatArticleSpiderIT {
    @Autowired
    WechatArticleSpider wechatArticleSpider;
//    public final static String URL = "http://weixin.sogou.com/api/share?timestamp=1553512239&signature=qIbwY*nI6KU9tBso4VCd8lYSesxOYgLcHX5tlbqlMR8N6flDHs4LLcFgRw7FjTAOuc3nGkPjCGSGzIM7pVy0GJDrt-hDy32qAUJvxEjqAXuYG23EnY5NUV6iuw6wtJpzfeQo1OWqacXp9r13qgRVt-3ZJD6jkUCCtLE1q1uLrjbhiuJnqATUhooObe2Vs1S1c6Ptrr5KtqACQZ52tP3JkJjBrMBgwVsWDfcZJ55pu7U=";
    public final static String URL = "http://mp.weixin.qq.com/s?src=11&timestamp=1611111674&ver=2839&signature=cbFQp33OhTsAaRtaya99E2z6b6bVDIkxXHuwoDQyz1F9FYXNIZz*3T8ImrlRwDtJk*mrEHdFWjdzO5ticvPL-82P8bSR0B7Ky5zpIRohz5ACEyG*d5YDCaGvxRdG06x0&new=1";
    @Test
    public void testProcess() {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(URL);
        job.setCode("");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(wechatArticleSpider)
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline)
                .addRequest(delayRequest)
                .run();
        ResultItems resultItems = testPipeline.getResultItems();

        Request request = resultItems.getRequest();
        Assert.isTrue(URL.equals(request.getUrl()));
        System.out.println("%%%%%%%%%%%%%%%%%test end%%%%%%%%%%%%%%%%%%%%%%");
    }

}
