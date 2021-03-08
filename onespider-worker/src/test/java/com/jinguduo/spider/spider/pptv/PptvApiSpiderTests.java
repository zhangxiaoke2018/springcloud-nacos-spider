package com.jinguduo.spider.spider.pptv;

import java.util.List;
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

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class PptvApiSpiderTests {

    @Value("classpath:json/pptvApiSpiderTests.json")
    private Resource rawText;
    
    @Autowired
    private PptvApiSpider pptvApiSpider;
    
    @Test
    public void testProcess() throws Exception {
        Job pcJob = new Job("http://epg.api.pptv.com/detail.api?cb=recDetailData&auth=d410fafa&vid=9040583");
        pcJob.setPlatformId(1);
        pcJob.setShowId(1);

        DelayRequest request = new DelayRequest(pcJob);
        
        Page page = new Page();
        page.setUrl(new PlainText("http://epg.api.pptv.com/detail.api?cb=recDetailData&auth=d410fafa&vid=9040583"));
        page.setRequest(request);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(IoResourceHelper.readResourceContent(rawText));

        pptvApiSpider.process(page);
        
        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);

        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs);
    }
}
