package com.jinguduo.spider.spider.sohu;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

import org.junit.Before;
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
public class SohuApiSpiderIT {

    @Autowired
    private SohuApiSpider sohuSpider;

    @Test
    public void testContext() {
        Assert.notNull(sohuSpider);
    }

    @Test
    public void testTotalPage()  {
        Job job = new Job("http://api.tv.sohu.com/v4/user/playlist.json?api_key=f351515304020cad28c92f70f002261c&user_id=283849056&page_size=20&is_pgc=1&sort_type=2");
        job.setPlatformId(1);
        job.setShowId(1);
        job.setFrequency(100);
        job.setCode("9429079");
        DelayRequest delayRequest = new DelayRequest(job);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.isTrue(showLogs.size()>=0);
        Assert.isTrue(showLogs.get(0).getPlayCount() > 0);
    }


}
