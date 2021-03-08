package com.jinguduo.spider.spider.kanmanhua;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.spider.kuaikan.KuaikanDetailSpider;
import com.jinguduo.spider.spider.sohu.SohuCommentSpider;
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
public class kanmanhuaSpiderIT {

    @Autowired
    private KanmanhuaSpider spider;


    @Test
    public void testDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://getconfig-globalapi.yyhao.com/app_api/v5/getcomicinfo_body/?comic_id=107327&platformname=android&productname=kmh");
        job.setCode("kan-17745");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ComicKuaiKan> comicKuaiKans = resultItems.get(ComicKuaiKan.class.getSimpleName());
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());

        Assert.isTrue(comicKuaiKans.size() > 0);
        Assert.isTrue(comics.size() > 0);
    }
}
