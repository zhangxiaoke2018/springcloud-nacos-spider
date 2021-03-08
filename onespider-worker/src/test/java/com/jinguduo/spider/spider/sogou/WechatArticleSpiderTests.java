package com.jinguduo.spider.spider.sogou;


import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.SougouWechatArticleText;
import com.jinguduo.spider.data.table.WeiboOfficialLog;
import com.jinguduo.spider.spider.weibo.WeiboOfficialApiSpider;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class WechatArticleSpiderTests {

    @Autowired
    private WechatArticleSpider wechatArticleSpider;

    final static String JSON_FILE = "/html/SougouWechatArticle.html";
    final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);
    final static String URL = "https://mp.weixin.qq.com/s/4lPPqgXTn8nd1jpKMXBMYg";

    DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(URL);

        // request
        delayRequest = new DelayRequest(job);
    }

    @Test
    public void testContext() {
        Assert.notNull(wechatArticleSpider);
    }

    @Test
    public void testProcessOk() throws Exception {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(RAW_TEXT);
        page.setUrl(new PlainText(URL));

        wechatArticleSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<SougouWechatArticleText> texts = resultItems.get(SougouWechatArticleText.class.getSimpleName());
        Assert.notNull(texts);
    }
    
}
