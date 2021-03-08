package com.jinguduo.spider.spider.pptv;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.HttpHelper;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;
import org.jsoup.select.Elements;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年9月13日 下午5:13:16
 */
@Worker
@Slf4j
public class PptvBannerSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("tv.pptv.com").build();

    private PageRule rule = PageRule.build().add("", page -> processPage(page));


//    private PageRule rules = PageRule.build()
//            .add("vicvUUrogkM4xrxc", page ->test(page));
//
//    private void test(Page page) {
//        Job job=((DelayRequest)page.getRequest()).getJob();
//        Document doc = Jsoup.parse(page.getRawText());
//        String title2 = doc.getElementsByClass("h3").first().attr("title");
//        String code = FetchCodeEnum.getCode(job.getUrl());
//        List<Show> shows = Lists.newArrayList();
//        List<Job> jobs = Lists.newArrayList();
//        List<AutoFindLogs> findLogs = Lists.newArrayList();
//        if(StringUtils.isNotBlank(title2)){
//            title = title2;
//        }
//        if(StringUtils.isBlank(code)){
//            return;
//        }
//
//        Show show = new Show(title,code,CommonEnum.Platform.PPTV.getCode(),0);
//        show.setCategory(Category.TV_DRAMA.name());
//        show.setUrl(job.getUrl());
//        show.setSource(3);//3-代表自动发现的剧
//        shows.add(show);
//        findLogs.add(new AutoFindLogs(title,Category.TV_DRAMA.name(),CommonEnum.Platform.PPTV.getCode(),job.getUrl(),code));
//
//        Job newJob = DbEntityHelper.deriveNewJob(job, job.getUrl());
//        newJob.setCode(code);
//        jobs.add(newJob);
//    }


    public void processPage(Page page) {

        Html html = page.getHtml();

        List<Selectable> nodes = html.$("#sliderBtns li").nodes();

        if (CollectionUtils.isEmpty(nodes)) {
            log.error("PptvAutoFindSpider result is empty!");
            return;
        }

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();

        nodes.stream().forEach(n -> save(n, oldJob, jobs, findLogs, shows, page));

        if (CollectionUtils.isNotEmpty(jobs)) {
            putModel(page, jobs);
        }
        if (CollectionUtils.isNotEmpty(findLogs)) {
            putModel(page, findLogs);
        }
        if (CollectionUtils.isNotEmpty(shows)) {
            putModel(page, shows);
        }
    }

    private void save(Selectable n, Job oldJob, List<Job> jobs, List<AutoFindLogs> findLogs, List<Show> shows, Page page) {
        String url = n.xpath("a//@href").get();

        Pattern p = Pattern.compile("v\\.pptv.com/show/\\S+\\.html");
        Matcher m = p.matcher(url);
        while (m.find()) {
            String title = n.xpath("a//@title").get();

            if (StringUtils.isNotBlank(url)) {
//                String res = HttpHelper.get(url, "UTF-8");
                Job njob = DbEntityHelper.derive(oldJob, new Job(url));
                njob.setPlatformId(11);
                String code = FetchCodeEnum.getCode(url);
                njob.setCode(code);
                njob.setFrequency(FrequencyConstant.FINDING_SHOW);
                putModel(page, njob);
                Document doc = Jsoup.parse(page.getRawText());
                Elements h3s = doc.getElementsByClass("h3");
                if (null != h3s && !h3s.isEmpty()){
                    String title2 = h3s.first().attr("title");
                    if (StringUtils.isNotBlank(title2)) {
                        title = title2;
                    }
                }

                if (StringUtils.isBlank(code)) {
                    return;
                }

                Show show = new Show(title, code, CommonEnum.Platform.PPTV.getCode(), 0);
                show.setCategory(Category.TV_DRAMA.name());
                show.setUrl(url);
                show.setSource(3);//3-代表自动发现的剧
                shows.add(show);
                findLogs.add(new AutoFindLogs(title, Category.TV_DRAMA.name(), CommonEnum.Platform.PPTV.getCode(), url, code));

                Job newJob = DbEntityHelper.deriveNewJob(oldJob, url);
                newJob.setCode(code);
                jobs.add(newJob);
            }
        }
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
