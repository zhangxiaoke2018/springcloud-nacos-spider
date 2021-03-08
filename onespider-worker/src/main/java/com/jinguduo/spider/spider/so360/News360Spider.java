package com.jinguduo.spider.spider.so360;

import org.apache.commons.lang3.StringUtils;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.table.News360Log;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class News360Spider extends CrawlSpider{

    private Site site = SiteBuilder.builder().setDomain("news.so.com").build();

    private PageRule rules = PageRule.build()
            .add("", page -> analyze360NewsInvolveCountProcess(page));

    private void analyze360NewsInvolveCountProcess(Page page) {
        log.debug("=========analyze  360NewsInvolveCount  process begin=========");

        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job == null) {
            log.debug("job is null");
            return;
        }

        String url = page.getUrl().get();
        //提及量
        Integer involveCount = -1;
        log.debug("360 news url :["+url+"]");
        String bh = page.getHtml().xpath("//*[@id=\"page\"]/span/text()").replace(",","").regex("([0-9]+)",1).get();
        if (StringUtils.isBlank(bh)){
            involveCount = 0;
        }else {
            involveCount = Integer.valueOf(bh);
        }

        //save media data
        News360Log news360Log = new News360Log();
        news360Log.setCode(job.getCode());
        news360Log.setCount(involveCount);
        putModel(page,news360Log);

        log.debug("=========analyze 360 news process end=========");
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
