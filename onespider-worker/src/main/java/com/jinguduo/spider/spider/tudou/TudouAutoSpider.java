package com.jinguduo.spider.spider.tudou;


import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

//已下架爬虫任务
//@Worker
@CommonsLog
@Deprecated
public class TudouAutoSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("movie.tudou.com").build();//http://movie.youku.com/PGC

    private PageRule rules = PageRule.build()
            .add("PGC",page -> processNewMovie(page));

    private void processNewMovie(Page page){
    	Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();

        Element content = document.getElementById("m959");

        if(content == null ){
            log.debug("页面无所需内容:"+page.getUrl().get());
            return;
        }

        Elements as = content.getElementsByClass("v2p");

        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        for (Element a : as) {
            Element na = a.getElementsByTag("a").get(0);
            String url = na.attr("href");
            String name = na.attr("title");
            log.debug(url + "      " + name);

                Job job = DbEntityHelper.derive(oldJob, new Job(url));
                job.setFrequency(FrequencyConstant.GENERAL_SHOW_INFO);
                job.setCode(FetchCodeEnum.getCode(url));
                job.setPlatformId(4);
                job.setShowId(0);
                jobs.add(job);

                Show show = new Show();
                show.setName(name);
                show.setCategory(Category.NETWORK_MOVIE.name());
                show.setCode(FetchCodeEnum.getCode(url));
                show.setPlatformId(4);
                show.setUrl(url);
                shows.add(show);
        }
        putModel(page,jobs);
        putModel(page,shows);
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
