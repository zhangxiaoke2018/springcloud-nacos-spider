package com.jinguduo.spider.spider.youku;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.ShowPopularLogs;
import com.jinguduo.spider.webmagic.ResultItems;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuHotCountSpiderIT {

    @Autowired
    private YoukuAcsDomainSpider youkuHotCountSpider;

    final static String JSON_FILE = "/json/youkuHotCount.json";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    final static String ALBUM_ID = "422060400";
    final static String URL = "http://www.iqiyi.com/v_19rrkchixo.html#vfrm=2-4-0-1";

    final static String DRAMA_URL ="https://acs.youku.com/h5/mtop.youku.haixing.play.h5.detail/1.0/?jsv=2.5.0&appKey=24679788&&v=1.0&type=originaljson&dataType=json&api=mtop.youku.haixing.play.h5.detail&data=%7B%22device%22%3A%22H5%22%2C%22layout_ver%22%3A%22100000%22%2C%22system_info%22%3A%22%7B%5C%22device%5C%22%3A%5C%22H5%5C%22%2C%5C%22pid%5C%22%3A%5C%220d7c3ff41d42fcd9%5C%22%2C%5C%22guid%5C%22%3A%5C%221547803393171S2M%5C%22%2C%5C%22utdid%5C%22%3A%5C%221547803393171S2M%5C%22%2C%5C%22ver%5C%22%3A%5C%221.0.0.0%5C%22%2C%5C%22userAgent%5C%22%3A%5C%22Mozilla%2F5.0+%28Linux%3B+Android+6.0%3B+Nexus+5+Build%2FMRA58N%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F71.0.3578.98+Mobile+Safari%2F537.36%5C%22%7D%22%2C%22video_id%22%3A%222006efbfbd54efbfbd5e%22%7D";

    final static String VARIETY_URL="https://acs.youku.com/h5/mtop.youku.haixing.play.h5.detail/1.0/?jsv=2.5.0&appKey=24679788&&v=1.0&type=originaljson&dataType=json&api=mtop.youku.haixing.play.h5.detail&data=%7B%22device%22%3A%22H5%22%2C%22layout_ver%22%3A%22100000%22%2C%22system_info%22%3A%22%7B%5C%22device%5C%22%3A%5C%22H5%5C%22%2C%5C%22pid%5C%22%3A%5C%220d7c3ff41d42fcd9%5C%22%2C%5C%22guid%5C%22%3A%5C%221547803393171S2M%5C%22%2C%5C%22utdid%5C%22%3A%5C%221547803393171S2M%5C%22%2C%5C%22ver%5C%22%3A%5C%221.0.0.0%5C%22%2C%5C%22userAgent%5C%22%3A%5C%22Mozilla%2F5.0+%28Linux%3B+Android+6.0%3B+Nexus+5+Build%2FMRA58N%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F71.0.3578.98+Mobile+Safari%2F537.36%5C%22%7D%22%2C%22video_id%22%3A%2266efbfbd13efbfbd144b%22%7D";

    final static String MOVIE_URL="https://acs.youku.com/h5/mtop.youku.haixing.play.h5.detail/1.0/?jsv=2.5.0&appKey=24679788&&v=1.0&type=originaljson&dataType=json&api=mtop.youku.haixing.play.h5.detail&data=%7B%22device%22%3A%22H5%22%2C%22layout_ver%22%3A%22100000%22%2C%22system_info%22%3A%22%7B%5C%22device%5C%22%3A%5C%22H5%5C%22%2C%5C%22pid%5C%22%3A%5C%220d7c3ff41d42fcd9%5C%22%2C%5C%22guid%5C%22%3A%5C%221547803393171S2M%5C%22%2C%5C%22utdid%5C%22%3A%5C%221547803393171S2M%5C%22%2C%5C%22ver%5C%22%3A%5C%221.0.0.0%5C%22%2C%5C%22userAgent%5C%22%3A%5C%22Mozilla%2F5.0+%28Linux%3B+Android+6.0%3B+Nexus+5+Build%2FMRA58N%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F71.0.3578.98+Mobile+Safari%2F537.36%5C%22%7D%22%2C%22video_id%22%3A%2266efbfbd13efbfbd144b%22%7D";

    final static String DANMU_URL ="https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/?jsv=2.5.1&appKey=24679788&api=mopen.youku.danmu.list&v=1.0&type=originaljson&dataType=jsonp&timeout=20000&jsonpIncPrefix=utility&data=%7B%22pid%22%3A0%2C%22ctype%22%3A10004%2C%22sver%22%3A%223.1.0%22%2C%22cver%22%3A%22v1.0%22%2C%22ctime%22%3A1598438216360%2C%22guid%22%3A%22EtW3F0kASSgCAXLz3WlAIEkl%22%2C%22vid%22%3A%22XNDczNDc5MDcwOA%3D%3D%22%2C%22mat%22%3A1%2C%22mcount%22%3A1%2C%22type%22%3A1%7D";
    final static String danmu_url ="https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/?jsv=2.5.1&appKey=24679788&api=mopen.youku.danmu.list&v=1.0&type=originaljson&dataType=jsonp&timeout=20000&jsonpIncPrefix=utility&data=%7B%22pid%22%3A0%2C%22ctype%22%3A10004%2C%22sver%22%3A%223.1.0%22%2C%22cver%22%3A%22v1.0%22%2C%22ctime%22%3A1598522471987%2C%22guid%22%3A%22EtW3F0kASSgCAXLz3WlAIEkl%22%2C%22vid%22%3A%22XNDczNDc5MDcwOA%3D%3D%22%2C%22mat%22%3A3%2C%22mcount%22%3A1%2C%22type%22%3A1%7D";
    DelayRequest delayRequest;


    @Test
    public void testContext() {
        Assert.notNull(youkuHotCountSpider);
        Assert.notNull(DANMU_URL);
    }

    @Test
    public void testZonyi(){

        Job j = new Job(danmu_url);
        j.setCode("XNDczNDc5MDcwOA==");
        j.setPlatformId(1);
        j.setShowId(1);
        j.setMethod("POST");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuHotCountSpider).addSpiderListeners(Lists.newArrayList(new YoukuAcsDomainDownLoaderListener())).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<ShowPopularLogs> shows = resultItems.get(ShowPopularLogs.class.getSimpleName());
        Assert.notEmpty(shows);
        Assert.notEmpty(jobs);
    }


    @Test
    public void testAutoFind(){
        String url = "https://acs.youku.com/h5/mtop.youku.haixing.play.h5.detail/1.0/?jsv=2.5.0&appKey=24679788&&v=1.0&type=originaljson&dataType=json&api=mtop.youku.haixing.play.h5.detail&data=%7b%22device%22%3a%22H5%22%2c%22layout_ver%22%3a%22100000%22%2c%22system_info%22%3a%22%7b%5c%22device%5c%22%3a%5c%22H5%5c%22%2c%5c%22pid%5c%22%3a%5c%220d7c3ff41d42fcd9%5c%22%2c%5c%22guid%5c%22%3a%5c%221547803393171S2M%5c%22%2c%5c%22utdid%5c%22%3a%5c%221547803393171S2M%5c%22%2c%5c%22ver%5c%22%3a%5c%221.0.0.0%5c%22%2c%5c%22userAgent%5c%22%3a%5c%22Mozilla%2f5.0+(Linux%3b+Android+6.0%3b+Nexus+5+Build%2fMRA58N)+AppleWebKit%2f537.36+(KHTML%2c+like+Gecko)+Chrome%2f71.0.3578.98+Mobile+Safari%2f537.36%5c%22%7d%22%2c%22video_id%22%3a%22XMzM1Mjc0OTk4MA%22%7d";

        Job j = new Job(url);
        j.setCode("z66efbfbd65efbfbd3919");
        j.setPlatformId(1);
        j.setShowId(1);
        j.setMethod("POST");

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuHotCountSpider).addSpiderListeners(Lists.newArrayList(new YoukuAcsDomainDownLoaderListener())).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

        List<ShowPopularLogs> showPopularLogs = resultItems.get(ShowPopularLogs.class.getSimpleName());
        Assert.notEmpty(showPopularLogs);
    }

}
