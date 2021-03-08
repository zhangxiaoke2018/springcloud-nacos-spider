
package com.jinguduo.spider.spider.youku;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuApiSpiderIT {

    @Autowired
    private YoukuApiSpider youkuApiSpider;

    @Test
    public void testContext() {
        Assert.notNull(youkuApiSpider);
    }

    @Test
    public void testZongyi() throws Exception {

        Job j = new Job("http://api.m.youku.com/api/showlist/getshowlist?vid=XMTQ4OTAwOTY2NA==&showid=302611&cateid=85&pagesize=1&page=0");
        j.setCode("z727f4876d18d11e69c81");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuApiSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Show> list = resultItems.get(Show.class.getSimpleName());
        
        Assert.notNull(list);
    }
}
