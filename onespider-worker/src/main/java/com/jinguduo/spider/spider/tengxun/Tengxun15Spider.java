package com.jinguduo.spider.spider.tengxun;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
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
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/28 下午1:22
 */
@Worker
@CommonsLog
public class Tengxun15Spider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("15.qq.com").setCharset("gb2312").build();
    
    private PageRule rule = PageRule.build().add("", page ->processPage(page));

    private final String ONE_EPISODE = "http://sns.video.qq.com/tvideo/fcgi-bin/batchgetplaymount?id=%s&otype=json";

    public void processPage(Page page) {

        Document document = page.getHtml().getDocument();

        Elements plays = document.getElementsByClass("video_bd");

        List<Job> jobs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();

        for (Element play : plays) {

            String title = play.getElementsByTag("p").text();
            String url = play.getElementsByTag("dl").get(0).attr("data-link");
            String code = play.getElementsByTag("dl").get(0).attr("id");

            log.debug("title:" + title + " ; length:"+title.length());

            if (StringUtils.isBlank(url)) {
                url = play.getElementsByTag("dl").get(0).attr("link");
            }

            Job oldJob = ((DelayRequest) page.getRequest()).getJob();
                if (oldJob != null) {
                    Job job = new Job();
                    DbEntityHelper.derive(oldJob, job);
                    job.setCode(code);
                    job.setUrl(String.format(ONE_EPISODE, code));
                    jobs.add(job);

                    Show show = new Show(title, code, job.getPlatformId(), oldJob.getShowId());
                    show.setDepth(2);
                    shows.add(show);
                }
        }
        putModel(page,jobs);
        putModel(page,shows);
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return this.rule;
    }
}
