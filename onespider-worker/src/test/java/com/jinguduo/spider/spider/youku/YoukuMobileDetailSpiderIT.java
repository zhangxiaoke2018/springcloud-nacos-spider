
package com.jinguduo.spider.spider.youku;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.ImmutableList;
import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuMobileDetailSpiderIT {

    @Autowired
    private YoukuMobileDetailSpider youkuMobileDetailSpider;

    @Test
    public void testContext() {
        Assert.notNull(youkuMobileDetailSpider, "Bad");
    }

    @Test
    public void testNetMovie() throws Exception {
        String url ="http://detail.mobile.youku.com/shows/z2b70efbfbd0fefbfbd39/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&imei=860123456789012&_t_=1526458086&mac=02:00:00:00:00:00&ver=6.4.7&e=md5&_s_=a52c553c9b668ea42fc2ad233b5f3728&operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8_46000&network=WIFI&fields=vid%7Ctitl%7Clim%7Cis_new%7Cpv%7Cimg&pg=1&pz=200&area_code=1";
        Job j = new Job(url);
        j.setCode("z2b70efbfbd0fefbfbd39");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuMobileDetailSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new YoukuMobileAppUrlSpiderListener()))
        	.addPipeline(testPipeline)
        	.addRequest(firstDelayRequest)
        	.run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> list = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(list, "Bad");
    }
    
    @Test
    public void testTheater() throws Exception {
    	String url = "http://detail.mobile.youku.com/shows/d4a1c61a5c114a6e89e9/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&imei=860123456789012&_t_=1526458086&mac=02:00:00:00:00:00&ver=6.4.7&e=md5&_s_=a52c553c9b668ea42fc2ad233b5f3728&operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8_46000&network=WIFI&fields=vid%7Ctitl%7Clim%7Cis_new%7Cpv%7Cimg&pg=1&pz=200&area_code=1";
        Job j = new Job(UriComponentsBuilder.fromHttpUrl(url).build().encode().toUriString());
        j.setCode("d4a1c61a5c114a6e89e9");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuMobileDetailSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new YoukuMobileAppUrlSpiderListener()))
        	.addPipeline(testPipeline)
        	.addRequest(firstDelayRequest)
        	.run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> list = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(list, "Bad");
    }
    
    @Test
    public void testTheater2() throws Exception {
        Job j = new Job("http://detail.mobile.youku.com/shows/XMjg2MDMwNTQ2MA/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&mac=02:00:00:00:00:00&imei=865441038043881&ver=6.4.7&_t_=1526374986&e=md5&_s_=1d4f64e810f582f0cc13fb25f6bcbf30&operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8_46000&network=WIFI&fields=vid%7Ctitl%7Clim%7Cis_new%7Cpv%7Cimg&pg=1&pz=200&area_code=1");
        j.setCode("XMjg2MDMwNTQ2MA");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuMobileDetailSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new YoukuMobileAppUrlSpiderListener()))
        	.addPipeline(testPipeline)
        	.addRequest(firstDelayRequest)
        	.run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> list = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(list, "Bad");
    }
    
    @Test
    public void testTheater3() throws Exception {
        Job j = new Job("http://detail.mobile.youku.com/shows/XMzc1Mzk2MzA3Mg==/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&mac=02:00:00:00:00:00&imei=865441038043881&ver=6.4.7&_t_=1526374986&e=md5&_s_=1d4f64e810f582f0cc13fb25f6bcbf30&operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8_46000&network=WIFI&fields=vid%7Ctitl%7Clim%7Cis_new%7Cpv%7Cimg&pg=1&pz=200&area_code=1");
        j.setCode("XMzc1Mzk2MzA3Mg==");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuMobileDetailSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new YoukuMobileAppUrlSpiderListener()))
        	.addPipeline(testPipeline)
        	.addRequest(firstDelayRequest)
        	.run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> list = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(list, "Bad");
    }
    
    @Test
    public void testNetDrama() throws Exception {
        Job j = new Job("http://detail.mobile.youku.com/shows/zd4a1c61a5c114a6e89e9/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&mac=02:00:00:00:00:00&imei=865441038043881&ver=6.4.7&_t_=1526374986&e=md5&_s_=1d4f64e810f582f0cc13fb25f6bcbf30&operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8_46000&network=WIFI&fields=vid%7Ctitl%7Clim%7Cis_new%7Cpv%7Cimg&pg=1&pz=200&area_code=1");
        j.setCode("zd4a1c61a5c114a6e89e9");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuMobileDetailSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new YoukuMobileAppUrlSpiderListener()))
        	.addPipeline(testPipeline)
        	.addRequest(firstDelayRequest)
        	.run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> list = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(list, "Bad");
    }
    
    @Test
    public void testNetDrama2() throws Exception {
    	// 使用网络综艺某一集的videoId
        Job j = new Job("http://detail.mobile.youku.com/shows/900029688/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&mac=02:00:00:00:00:00&imei=865441038043881&ver=6.4.7&_t_=1526374986&e=md5&_s_=1d4f64e810f582f0cc13fb25f6bcbf30&operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8_46000&network=WIFI&fields=vid%7Ctitl%7Clim%7Cis_new%7Cpv%7Cimg&pg=1&pz=200&area_code=1");
        j.setCode("zd4805b2fefbfbd4e11ef");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuMobileDetailSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new YoukuMobileAppUrlSpiderListener()))
        	.addPipeline(testPipeline)
        	.addRequest(firstDelayRequest)
        	.run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> list = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(list, "Bad");
    }
    
    @Test
    public void testNetDrama3() throws Exception {
    	// 使用网络综艺某一集的编码后的videoId
        Job j = new Job("http://detail.mobile.youku.com/shows/XMzc1Mzk2MzA3Mg==/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&imei=860123456789012&_t_=1526458086&mac=02:00:00:00:00:00&ver=6.4.7&e=md5&_s_=a52c553c9b668ea42fc2ad233b5f3728&operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8_46000&network=WIFI&fields=vid%7Ctitl%7Clim%7Cis_new%7Cpv%7Cimg&pg=1&pz=200&area_code=1");
        j.setCode("XMzc1Mzk2MzA3Mg==");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuMobileDetailSpider)
        	.setDownloader(new DownloaderManager())
        	.addSpiderListeners(ImmutableList.of(new YoukuMobileAppUrlSpiderListener()))
        	.addPipeline(testPipeline)
        	.addRequest(firstDelayRequest)
        	.run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<ShowLog> list = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notEmpty(list, "Bad");
    }
}
