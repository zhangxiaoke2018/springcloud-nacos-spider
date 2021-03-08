package com.jinguduo.spider.spider.taomi;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ShowLog;
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
public class TaomiPlayCountAnimeSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("vapp.61.com")  //http://vapp.61.com/api.php?method=api.Score.getVideoInfo&vid=10530
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("getVideoInfo",page -> processAnime(page));

    private void processAnime(Page page) {

        log.debug("process taomi anime play count " + page.getUrl().get());

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }
        Long playCount = JSONObject.parseObject(page.getRawText().substring(1,page.getRawText().length() - 1)).getLong("total_v");

        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setPlayCount(playCount);

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
