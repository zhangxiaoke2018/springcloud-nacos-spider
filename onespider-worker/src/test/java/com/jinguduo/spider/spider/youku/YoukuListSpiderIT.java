
package com.jinguduo.spider.spider.youku;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

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
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class YoukuListSpiderIT {

    @Autowired
    private YoukuListSpider youkuSpider;
    
    private static final String MOVIE_URL = "https://list.youku.com/category/show/c_96_u_1_s_5_d_1.html";
    private static final String DRAMA_URL = "https://list.youku.com/show/id_z933d6904c62411e4b522.html";
    private static final String VARIETY_URL = "https://list.youku.com/category/show/c_85_r__u_2_s_5_d_1.html";

    private static final String KID_ANIME = "https://list.youku.com/category/show/c_177_g_动画_s_6_d_1_p_15_a_中国.html";

    //国产少儿动漫
    private static final String CN_KID_ANIME="https://list.youku.com/category/page?c=100&s=6&d=1&a=日本&type=show&p=1";
    //网大
    private static final String NETWORK_MOVIE="https://list.youku.com/category/show/c_96_u_1_s_5_d_1.html";
    //国外少儿动画电影
    private static final String UA_KID_ANIME_MOVIE="https://list.youku.com/category/show/c_96_g_动画_a_美国_s_6_d_1_p_1_pt_.html";

    private static final String JAPAN_ANIME ="https://list.youku.com/show/id_z2b70efbfbd0fefbfbd39.html";
    @Test
    public void testContext() {
        Assert.notNull(youkuSpider);
    }

    @Test
    public void testAutoFind(){
        Job j = new Job("https://list.youku.com/show/id_zdbfeb7809a8244278e8e.html");
        j.setCode("zdbfeb7809a8244278e8e");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        Assert.notEmpty(shows);
        Assert.notEmpty(jobs);
        log.debug(shows.size());
    }

    /**
     * 视频专题页processTest
     * @
     */
    @Test
    public void testPageGet1()  {

        String url = "https://list.youku.com/category/show/c_177_g_动画_s_6_d_1_p_1_a_中国.html";

        Job j = new Job(url);
        j.setCode(FetchCodeEnum.getCode(url));
        j.setPlatformId(1);
        j.setShowId(1);
        
        DelayRequest firstDelayRequest = new DelayRequest(j);
        
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs);
        log.debug(showLogs);
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        for (Job job : jobs) {
            log.debug(job);
        }

    }

    /**
     * 生成分集任务
     * @
     */
    @Test
    public void tabs()  {

        Job j = new Job("http://list.youku.com/show/module?id=464907&tab=point&callback=jQuery");
        j.setCode("zdbfeb7809a8244278e8e");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
        jobs.forEach(s -> log.debug(s));
    }

    @Test
    public void testEpiShows()  {
        Job j = new Job("http://list.youku.com/show/module?id=504306&tab=showInfo&callback=jQuery");
        j.setCode("z2b70efbfbd0fefbfbd39");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Show> shows = resultItems.get(Show.class.getSimpleName());
        Assert.notNull(shows);
        Assert.notNull(shows.get(0).getEpisode());
        shows.forEach(s -> log.debug(s));
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        jobs.forEach(s -> log.debug(s));
    }

    @Test
    public void testEpiShows2()  {
        Job j = new Job("http://list.youku.com/show/point?id=302611&stage=reload_1&callback=jQuery");
        j.setCode("z9b23954ce04c11e5b522");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        jobs.forEach(s -> log.debug(s));
    }

    @Test
    public void testPC()  {

        Job j = new Job("https://list.youku.com/show/id_2006efbfbd54efbfbd5e.html");
        j.setCode(FetchCodeEnum.getCode("http://list.youku.com/show/id_zc40df6c08e9211e5b522.html"));
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest delayRequest = new DelayRequest(j);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs);
        Assert.isTrue(showLogs.get(0).getPlayCount()>200000000);
        showLogs.forEach(s -> log.debug(s.getPlayCount()));
    }
@Test
    public void testMoviePC()  {

        Job j = new Job("http://list.youku.com/show/id_z1a9a04e88dd011e59e2a.html#movie_play_count");
        j.setCode("XMTM4MjQ0ODA4OA==");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest delayRequest = new DelayRequest(j);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs);

    }

    @Test
    public void testEpisodeJobs() {
        Job j = new Job("https://list.youku.com/show/id_zcdaced4474c24aa49db1.html");
        j.setCode(FetchCodeEnum.getCode("https://list.youku.com/show/id_zcdaced4474c24aa49db1.html"));
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest delayRequest = new DelayRequest(j);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
    }

    @Test
    public void testEpisodeShows() {
        Job j = new Job("http://list.youku.com/show/module?id=443576&tab=point&callback=jQuery");
        j.setCode(FetchCodeEnum.getCode("https://list.youku.com/show/id_zcdaced4474c24aa49db1.html"));
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest delayRequest = new DelayRequest(j);
        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
    }

    /***
     * 获取专辑下页，每月下分集List
     * page : "<div class="tab-c" id="showInfo"></div>"
     * @since 2017.02.14
     */
    @Test
    public void testEpisode()  {
        Job j = new Job("https://list.youku.com/show/module?id=483934&tab=showInfo&callback=jQuery");
        j.setCode("zcbdf88438c3249909d71");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notNull(jobs);
        jobs.forEach(s -> log.debug(s));
    }

    @Test
    public void testZongyi() throws Exception {

        Job j = new Job("https://list.youku.com/show/id_ze438e14b3fa94613a1bb.html");
        j.setCode("ze438e14b3fa94613a1bb");
        j.setPlatformId(1);
        j.setShowId(1);

        DelayRequest firstDelayRequest = new DelayRequest(j);

        TestPipeline testPipeline = new TestPipeline();
        SpiderEngine.create(youkuSpider).addPipeline(testPipeline).addRequest(firstDelayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();

    }
}
