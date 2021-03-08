package com.jinguduo.spider.spider.acfan;



import lombok.extern.apachecommons.CommonsLog;

import org.jsoup.nodes.Document;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 09/11/2016 6:07 PM
 */
@Worker
@CommonsLog
public class AcfanAnimeSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("www.acfun.tv").build();//http://www.acfun.tv/v/ab1470468_1

    private final String PLAY_COUNT_URL = "http://www.acfun.cn/bangumi/count/bangumi_view.aspx?bangumiId=%s";

    private PageRule rules = PageRule.build()
            .add("/v/",page -> processAnime(page))
            .add("/count/", page -> processPlayCount(page));

    private void processAnime(Page page){

        log.debug("process acfan anime " + page.getUrl().get());

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }

        Document document = page.getHtml().getDocument();
        if(document.getElementById("block-data-view")==null){
            return;
        }

        String bangumiId = document.getElementById("block-data-view").attr("data-id");

        Job newJob = new Job(String.format(PLAY_COUNT_URL,bangumiId));

        DbEntityHelper.derive(oldJob, newJob);
        newJob.setFrequency(FrequencyConstant.GENERAL_PLAY_COUNT);

        putModel(page,newJob);
    }

    private void processPlayCount(Page page){

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }

        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setPlayCount(NumberHelper.parseLong(page.getRawText(),-1));

        putModel(page,showLog);


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
