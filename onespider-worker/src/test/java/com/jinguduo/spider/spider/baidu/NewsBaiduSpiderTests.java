package com.jinguduo.spider.spider.baidu;


import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.BaiduNewsLog;
import com.jinguduo.spider.data.table.NewsArticleLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class NewsBaiduSpiderTests {
    @Autowired
    private NewsBaiduSpider newsBaiduApiSpider;

    @Test
    public void testProcessOk() throws Exception {
        String url = "http://news.baidu.com/ns?ct=1&rn=20&ie=utf-8&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=newstitle&word=%E7%8B%90%E7%8B%B8%E7%9A%84%E5%A4%8F%E5%A4%A9";
        String htmlFile = "/html/BaiduNews.html";
        String rawText = IoResourceHelper.readResourceContent(htmlFile);
        
        Job job = new Job();
        job.setUrl(url);
        job.setCode("xxxxxx");
        
        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);
        page.setUrl(new PlainText(url));

        newsBaiduApiSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "Bad");

        List<BaiduNewsLog> baiduNewsLog = resultItems.get(BaiduNewsLog.class.getSimpleName());
        Assert.notEmpty(baiduNewsLog, "Bad");
        Assert.isTrue(64700 == baiduNewsLog.get(0).getCount().intValue(), "Bad");
        
        List<NewsArticleLog> articles = resultItems.get(NewsArticleLog.class.getSimpleName());
        Assert.notEmpty(articles, "Bad");

    }
}
