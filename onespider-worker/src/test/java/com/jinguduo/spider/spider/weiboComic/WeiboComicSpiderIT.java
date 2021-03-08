package com.jinguduo.spider.spider.weiboComic;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.spider.kanmanhua.KanmanhuaSpider;
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
public class WeiboComicSpiderIT {

    @Autowired
    private WeiboComicSpider spider;


    @Test
    public void testCreatTask()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://apiwap.vcomic.com");
        job.setCode("wb-69000");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ComicKuaiKan> comicKuaiKans = resultItems.get(ComicKuaiKan.class.getSimpleName());
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());

        Assert.isTrue(comicKuaiKans.size() > 0);
        Assert.isTrue(comics.size() > 0);
    }


    @Test
    public void testComic()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://apiwap.vcomic.com/wbcomic/comic/filter_result?page_num=1&rows_num=100&cate_id=0&end_status=0&comic_pay_status=0&order=comic_read_num&_request_from=pc");
        job.setCode("wb-69000");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ComicKuaiKan> comicKuaiKans = resultItems.get(ComicKuaiKan.class.getSimpleName());
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());

        Assert.isTrue(comicKuaiKans.size() > 0);
        Assert.isTrue(comics.size() > 0);
    }

    @Test
    public void testComicDetail()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://apiwap.vcomic.com/wbcomic/comic/comic_show?comic_id=68871&_request_from=pc");
        job.setCode("wb-69000");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(spider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ComicKuaiKan> comicKuaiKans = resultItems.get(ComicKuaiKan.class.getSimpleName());
        List<Comic> comics = resultItems.get(Comic.class.getSimpleName());

        Assert.isTrue(comicKuaiKans.size() > 0);
        Assert.isTrue(comics.size() > 0);
    }
}
