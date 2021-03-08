package com.jinguduo.spider.spider.sohu;

import org.apache.http.client.config.CookieSpecs;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/8 下午2:21
 */
@Worker
public class SohuPlayCountSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("count.vrs.sohu.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("queryext.action", page -> getCount(page));

    private void getCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String text = page.getRawText();
        if (text.indexOf("total") <= 0) {
        	throw new PageBeChangedException(text);
        }
        final int idx = text.lastIndexOf("total\":") + 7;
        Long playCount = NumberHelper.parseLong(text.substring(idx, text.indexOf(",", idx)), -1);

        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(job,showLog);
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
