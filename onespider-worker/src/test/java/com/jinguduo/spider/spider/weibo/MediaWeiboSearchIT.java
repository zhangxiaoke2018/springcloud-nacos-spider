package com.jinguduo.spider.spider.weibo;



import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.WeiboText;
import com.jinguduo.spider.webmagic.ResultItems;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class MediaWeiboSearchIT {
    @Autowired
    private WeiboSearchMobileSpider weiboSearchSpider;

    @Autowired
    private WeiboSearchSpider weiboSearchSpiderPC;

    public final static String URL = "https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D1%26q%3D%E6%A0%A1%E8%BD%A6%2B%E4%B9%A6#children_book";
    public final static String TAG_URL = "https://m.weibo.cn/api/container/getIndex?jumpfrom=weibocom&display=0&retcode=6102&containerid=1008083c39dbe93067de66517602c05f9ebb9b#%E8%82%96%E7%87%95";

    public final static String PC_URL = "https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=%s";


    @Before
    public void setup()  {

    }

    @Test
    public void PCSearchTest() throws InterruptedException {
        List<String> urlList = Lists.newArrayList();
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=1");
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=2");
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=3");
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=4");
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=5");
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=6");
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=7");
        urlList.add("https://s.weibo.com/weibo/%E6%9C%89%E7%BF%A1?topnav=1&wvr=6&b=1&page=8");
        for(String url:urlList) {
            TestPipeline testPipeline = new TestPipeline();
            Job job = new Job();
            job.setUrl(url);
            job.setCode("");
            DelayRequest delayRequest = new DelayRequest(job);

            SpiderEngine.create(weiboSearchSpiderPC)
                    .setDownloader(new DownloaderManager())
                    .addPipeline(testPipeline)
                    .addRequest(delayRequest)
                    .run();

            ResultItems resultItems = testPipeline.getResultItems();

            List<WeiboText> weiboTextList = resultItems.get(WeiboText.class.getSimpleName());

            Assert.isTrue(weiboTextList.size() > 0);
            Thread.sleep(10000);
        }


    }


    @Test
    public void testContext() {
        Assert.notNull(weiboSearchSpider);
    }

    //
    @Test
    public void searchTest()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl(TAG_URL);
        job.setCode("");
        DelayRequest delayRequest = new DelayRequest(job);

        SpiderEngine.create(weiboSearchSpider)
                .setDownloader(new DownloaderManager())
                .addPipeline(testPipeline)
                .addRequest(delayRequest)
                .run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<WeiboText> weiboTextList = resultItems.get(WeiboText.class.getSimpleName());

        Assert.isTrue(weiboTextList.size() > 0);


    }


}
