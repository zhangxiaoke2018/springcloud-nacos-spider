package com.jinguduo.spider.spider.mgtv;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import com.alibaba.fastjson.JSONObject;
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
 * @DATE 16/7/3 上午8:32
 */
@Worker
@CommonsLog
public class MgtvShowPageSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("v.api.hunantv.com").build();
    
    private PageRule rule = PageRule.build().add("", page -> processPage(page));

    private final String num = "http://videocenter-2039197532.cn-north-1.elb.amazonaws.com.cn//dynamicinfo?callback=jQuery18209559400354382939_1466759714593&vid=%s&_=1466759715614";

    public void processPage(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }

        JSONObject jsons = JSONObject.parseObject(page.getRawText().substring(page.getRawText().indexOf("(") + 1, page.getRawText().lastIndexOf(")")));

        List<JSONObject> items = (List<JSONObject>)jsons.getJSONObject("data").get("latest");

        List<Job> jobs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();

        for (JSONObject item  : items) {

            Job newJob = null;
                newJob = new Job(String.format(num, item.getString("video_id")));
            DbEntityHelper.derive(oldJob, newJob);
            newJob.setCode(item.getString("video_id"));
            jobs.add(newJob);

            Show show = new Show(item.getString("title"), item.getString("video_id"), oldJob.getPlatformId(),
                    oldJob.getShowId());
            show.setDepth(2);
            shows.add(show);
        }
        putModel(page,shows);
        putModel(page,jobs);

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
