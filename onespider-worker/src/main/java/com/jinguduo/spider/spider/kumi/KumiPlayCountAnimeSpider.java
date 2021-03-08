package com.jinguduo.spider.spider.kumi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
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
public class KumiPlayCountAnimeSpider extends CrawlSpider {

    private static final Logger log = LoggerFactory.getLogger(KumiPlayCountAnimeSpider.class);

    private Site site = SiteBuilder.builder().setDomain("list.kumi.cn").build();//http://list.kumi.cn/num.php?contentid=85063


    private PageRule rules = PageRule.build()
            .add("num.php",page -> processAnime(page));

    private void processAnime(Page page) {

        log.debug("process kumi anime play count " + page.getUrl().get());

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }
        String playCount = JSONObject.parseArray(page.getRawText().substring(1,page.getRawText().length() - 1)).getString(0).replace(",","");

        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setPlayCount(NumberHelper.parseLong(playCount,-1));

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
