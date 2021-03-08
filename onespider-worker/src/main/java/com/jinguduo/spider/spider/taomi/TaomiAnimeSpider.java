package com.jinguduo.spider.spider.taomi;



import org.jsoup.nodes.Document;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 09/11/2016 6:07 PM
 */
@Worker
@CommonsLog
public class TaomiAnimeSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("v.61.com")  //http://v.61.com/comic/10530/
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private final String PLAY_COUNT_URL = "http://vapp.61.com/api.php?method=api.Score.getVideoInfo&vid=%s";

    private PageRule rules = PageRule.build()
            .add("/comic/",page -> processAnime(page));

    private void processAnime(Page page){

        log.debug("process taomi anime " + page.getUrl().get());

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }

        Document document = page.getHtml().getDocument();

        String vid = document.getElementById("J_episodeInfo").attr("episode");

            Job newJob = new Job(String.format(PLAY_COUNT_URL,vid));

            DbEntityHelper.derive(oldJob, newJob);
            newJob.setFrequency(FrequencyConstant.GENERAL_PLAY_COUNT);

            putModel(page,newJob);
    }



    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
