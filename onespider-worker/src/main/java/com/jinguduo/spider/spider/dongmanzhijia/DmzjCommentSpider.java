package com.jinguduo.spider.spider.dongmanzhijia;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.data.table.ComicDmzj;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/4
 * Time:18:00
 */
@Slf4j
@Worker
public class DmzjCommentSpider extends CrawlSpider {
    private Site site = SiteBuilder.builder()
            .setDomain("interface.dmzj.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/NewComment2", page -> analyze(page));

    private void analyze(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        //返回数据为json格式转换为map
        JSONObject json = JSONObject.parseObject(page.getJson().get());

        Integer commentCount = json.getInteger("data");

        ComicDmzj dmzj = new ComicDmzj();
        dmzj.setCode(job.getCode());
        dmzj.setDay(DateHelper.getTodayZero(Date.class));
        dmzj.setCommentCount(commentCount);

        putModel(page, dmzj);



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
