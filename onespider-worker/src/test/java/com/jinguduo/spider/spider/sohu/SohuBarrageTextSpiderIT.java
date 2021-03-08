package com.jinguduo.spider.spider.sohu;


import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.ResultItems;

/**
 * Created by gsw on 2016/10/24.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SohuBarrageTextSpiderIT {

    @Autowired
    private SohuBarrageTextSpider sohuSearchSpider;

    @Test
    public void searchBarrageContentTest()  {
        Job newJob = new Job("http://api.danmu.tv.sohu.com/danmu?act=dmlist_v2&vid=2875714&page=1&pct=2&request_from=sohu_vrs_player&o=4&aid=8997103");
        newJob.setPlatformId(1);
        newJob.setShowId(1);
        newJob.setCode("9182738");
        DelayRequest delayRequest = new DelayRequest(newJob);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(sohuSearchSpider).addPipeline(testPipeline).addRequest(delayRequest).run();
        ResultItems resultItems = testPipeline.getResultItems();
        List<BarrageText> danmuLogs = resultItems.get(BarrageText.class.getSimpleName());
        Assert.isTrue(CollectionUtils.isNotEmpty(danmuLogs));
        
        BarrageText barrageText = danmuLogs.get(0);
        Assert.notNull(barrageText);
        Assert.notNull(barrageText.getBarrageId());
        Assert.notNull(barrageText.getPlatformId());
        Assert.notNull(barrageText.getCode());
    }
}
