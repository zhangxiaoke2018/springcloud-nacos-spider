package com.jinguduo.spider.spider.sohu;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.PlainText;

@Worker
public class SohuMyMediaSinglePlayCountSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("vstat.my.tv.sohu.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("/dostat\\.do\\?method=getVideoPlayCount&v=.*?&n=_stat", page -> single(page));

    private void single(Page page) {
        Job mainJ = ((DelayRequest) page.getRequest()).getJob();

        String raw = page.getRawText();

        if (StringUtils.isBlank(raw)){
            return;
        }
            //分集播放量
            Long playCount = NumberHelper.parseLong(new PlainText(raw).regex("\"count\":(.*?),",1).get(),1);
            ShowLog showLog = new ShowLog();
            DbEntityHelper.derive(mainJ, showLog);
            showLog.setPlayCount(playCount);
            putModel(page, showLog);

    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
