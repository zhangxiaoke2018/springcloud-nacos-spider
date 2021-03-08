package com.jinguduo.spider.spider.u17;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/10/17
 * Time:19:15
 */
@Worker
@CommonsLog
public class U17ComicRankSpider extends CrawlSpider {
    private Site site = SiteBuilder.builder().setDomain("comic.u17.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("t2.html$", page -> pagerProcess(page))
            .add("page", page -> createDetailJob(page));

    private final Integer MAX_PAGE = 50;
    private final String PAGE_URL = "http://comic.u17.com/rank/t2.html?page=%s";

    //创建分页任务
    private void pagerProcess(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        for (int i = 1; i <= MAX_PAGE; i++) {
            Job newJob = new Job(String.format(PAGE_URL, i));
            DbEntityHelper.derive(job, newJob);
            putModel(page, newJob);
        }
    }

    //生成具体的漫画详情页
    private void createDetailJob(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();
        Elements imgBoxs = document.getElementsByClass("img_box");
        for (Element imgBox : imgBoxs) {
            String href = imgBox.getElementsByClass("name").attr("href").replace("?source=rank", "");
            Job newJob = new Job(href);
            DbEntityHelper.derive(job, newJob);
            newJob.setCode("u17-" + StringUtils.substringBefore(StringUtils.substringAfterLast(href, "/"), ".html"));
            putModel(page, newJob);
        }
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

