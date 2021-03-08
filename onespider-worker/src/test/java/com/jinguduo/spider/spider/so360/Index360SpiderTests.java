package com.jinguduo.spider.spider.so360;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import static com.jinguduo.spider.data.table.Category.NETWORK_VARIETY;

import java.net.MalformedURLException;

import java.net.URL;

/**
 * Created by lc on 2017/5/4.
 */

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class Index360SpiderTests {
    @Autowired
    private Index360Spider index360Spider;

    private final Logger log = LoggerFactory.getLogger(Index360SpiderTests.class);
    //  final static String JSON_FILE = "/html/News360.html";
    //   final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

    DelayRequest delayRequest;

    // @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        //job.setUrl("http://index.haosou.com/result/trend?keywords=%E4%BA%BA%E6%B0%91%E7%9A%84%E5%90%8D%E4%B9%89");
        // job.setUrl("http://index.haosou.com/index/soIndexJson?area=%E5%85%A8%E5%9B%BD&q=%E4%BA%BA%E6%B0%91%E7%9A%84%E5%90%8D%E4%B9%89");
        job.setUrl("http://index.haosou.com/index/soMediaJson?q=%E4%BA%BA%E6%B0%91%E7%9A%84%E5%90%8D%E4%B9%89");
        String s = StringUtils.substringAfterLast(job.getUrl(), "q=");
        if (StringUtils.isNotBlank(s)) {
            s = StringUtils.substringBefore(s, "&");
        }

        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        String url = "http://www.toutiao.com/search_content/?offset=20&format=json&keyword=择天记&autoload=true&count=20&cur_tab=1";
        StringBuffer buffer = new StringBuffer(url);
        int i = buffer.indexOf("offset=");
        int j = buffer.indexOf("&format=");
        String num = buffer.substring(i + 7, j);
        Integer oldNum = Integer.valueOf(num);
        StringBuffer replace = buffer.replace(i + 7, j, String.valueOf(oldNum+20));
    }


    @Test
    public void testProcessOk() {

    }

}
