package com.jinguduo.spider.spider.so360;


import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * todo 第一部分开发，第二部分未开发
 */
//@Worker
@CommonsLog
public class KanKanSpider extends CrawlSpider {

    final static String DOMAIN = "http://www.360kan.com%s";

    private Site site = SiteBuilder.builder().setDomain("360kan.com").build();

    private PageRule rules = PageRule.build()
            .add("dianying", page -> list(page))
            .add("/m/", page -> category(page));

    private void list(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        List<Job> jobs = Lists.newArrayList();

        Html html = page.getHtml();

        List<Selectable> lis = html.xpath("//div[@class='b-listtab-main']/ul/li").nodes();

        if (CollectionUtils.isEmpty(lis)) {
            log.debug("download page no any result!");
            return;
        }

        lis
                .stream()
                .forEach(li -> save(li,oldJob,jobs))
        ;

        if(CollectionUtils.isNotEmpty(jobs)){
            putModel(page,jobs);
        }

    }

    /***
     * 按照平台分类插入任务
     * @param page
     */
    private void category(Page page) {


    }

    /** 保存 */
    private void save(Selectable s, Job old, List<Job> jobs) {

        Selectable a = s.xpath("//a[@class='js-tongjic']");

        //基础数据
        String url = a.xpath("a/@href").get();
        String code = a.xpath("a/@href").regex("\\/m\\/(\\S*)\\.html",1).get();

        //Job（搜索页）
        Job newJob = null;
            newJob = new Job(url);
        DbEntityHelper.derive(old,newJob);
        newJob.setCode(code);
        newJob.setFrequency(FrequencyConstant.GENERAL_SHOW_INFO);

        jobs.add(newJob);
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
