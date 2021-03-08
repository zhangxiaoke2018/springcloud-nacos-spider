package com.jinguduo.spider.spider.kanmanhua;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicKanmanhua;
import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.webmagic.ResultItems;
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
public class ComicKanmanhuaSpiderIT {

    @Autowired
    private ComicKanmanhuaSpider spider;


    @Test
    public void testDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://rankdata-globalapi.321mh.com/app_api/v1/comic/getRankDataDetials/?sort_type=all&product_id=1&rank_type=heat&time_type=total&query_time=2018-11-27&platformname=android&productname=kmh");
        job.setCode("kan-25934");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ComicKuaiKan> comicKuaiKans = resultItems.get(ComicKuaiKan.class.getSimpleName());
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());

        Assert.isTrue(comicKuaiKans.size() > 0);
        Assert.isTrue(comics.size() > 0);
    }

}
