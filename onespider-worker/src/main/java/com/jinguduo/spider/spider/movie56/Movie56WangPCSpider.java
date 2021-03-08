package com.jinguduo.spider.spider.movie56;

import org.apache.log4j.Logger;

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
 * @DATE 16/7/15 下午3:09
 */
@Worker
public class Movie56WangPCSpider extends CrawlSpider {

    private static final Logger LOGGER = Logger.getLogger(Movie56WangPCSpider.class);

    private Site site = SiteBuilder.builder().setDomain("vstat.v.blog.sohu.com").build();

    private PageRule rules = PageRule.build()
            .add("/dostat.do",page -> processMoviePC(page))
            .add("/vv/",page -> processExMoviePC(page));

    private void processMoviePC(Page page){
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String playcount = page.getRawText().substring(page.getRawText().indexOf("count")+7,page.getRawText().lastIndexOf(","));
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(job,showLog);
        showLog.setPlayCount(NumberHelper.parseLong(playcount,-1));

        putModel(page,showLog);
    }

    private void processExMoviePC(Page page){

        Job job = ((DelayRequest) page.getRequest()).getJob();
        String playcount = page.getRawText();
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(job,showLog);
        showLog.setPlayCount(NumberHelper.parseLong(playcount,-1));

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
