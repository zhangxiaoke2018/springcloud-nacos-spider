package com.jinguduo.spider.spider.mgtv;


import java.util.List;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;

import lombok.extern.apachecommons.CommonsLog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 29/12/2016 7:00 PM
 */
@Worker
@CommonsLog
public class MgtvEpiSpider extends CrawlSpider {

    private Site sites = SiteBuilder.builder().setDomain("pcweb.api.mgtv.com").build();

    private static final String START_STR = "http://www.mgtv.com";

    private static final String COMMENT_URL = "http://comment.mgtv.com/video_comment/list/?subject_id=%s&page=1";

    private static final String EPISODE_PLAYCOUNT_URL ="http://videocenter-2039197532.cn-north-1.elb.amazonaws.com.cn//dynamicinfo?callback=jQuery18209559400354382939_1466759714593&vid=%s";

    private PageRule rules = PageRule.build()
            .add("", page -> processEpi(page))
            .add("variety", page -> processZongYi(page));


    private void processZongYi(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String url = oldJob.getUrl();
        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        JSONObject data = jsonObject.getJSONObject("data");

        if (!url.contains("month")) {//说明是由详情页生成的分集api任务,需要创建其他月份的分集任务
            JSONArray tabM = data.getJSONArray("tab_m");
            for (int x = 0; x < tabM.size(); x++) {
                JSONObject epiM = tabM.getJSONObject(x);

                Job job = new Job(url + "&month=" + epiM.getString("m"));
                DbEntityHelper.derive(oldJob, job);
                putModel(page, job);
            }
        }

        JSONArray list = data.getJSONArray("list");
        for (int i = 0; i < list.size(); i++) {
            JSONObject epiShow = list.getJSONObject(i);

            Show show = new Show();
            show.setName(epiShow.getString("t1"));
            //过滤花絮片段
            if (!epiShow.getString("t2").matches("\\d+-\\d+-\\d+")) {
                return;
            }
            show.setEpisode(Integer.valueOf(epiShow.getString("t2").replace("-", "")));
            show.setCode(epiShow.getString("video_id"));
            show.setDepth(2);
            show.setPlatformId(oldJob.getPlatformId());
            show.setParentCode(oldJob.getCode());
            String isNew = epiShow.getString("isnew");// 暂时发现=2为预告片
            if (!"2".equals(isNew)) {
                putModel(page, show);
            }

            Job job = new Job(START_STR + epiShow.getString("url"));
            DbEntityHelper.derive(oldJob, job);
            job.setCode(epiShow.getString("video_id"));
            putModel(page, job);

            Job commentJob = new Job(String.format(COMMENT_URL, epiShow.getString("video_id")));
            DbEntityHelper.derive(oldJob, commentJob);
            commentJob.setCode(epiShow.getString("video_id"));
            putModel(page, commentJob);

            Job barrageJob = MgExtend.barrageTextJob(epiShow.getString("video_id"),epiShow.getString("clip_id"));
            DbEntityHelper.derive(oldJob, barrageJob);
            barrageJob.setCode(epiShow.getString("video_id"));
            putModel(page, barrageJob);

//            Job epiPlayCountJob = new Job(String.format(EPISODE_PLAYCOUNT_URL,epiShow.getString("video_id")));
//            DbEntityHelper.derive(oldJob, epiPlayCountJob);
//            epiPlayCountJob.setCode(epiShow.getString("video_id"));
//            putModel(page,epiPlayCountJob);

        }
    }

    private void processEpi(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String url = page.getUrl().get();

        if (url.contains("variety")) return;

        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        JSONObject data = jsonObject.getJSONObject("data");
        Integer currentPage = data.getInteger("current_page");
        Integer totalPage = data.getInteger("total_page");
        List jobs = Lists.newArrayList();
        List shows = Lists.newArrayList();

        if (currentPage < totalPage) {
            Job epiJob = new Job(url.substring(0, url.lastIndexOf("=") + 1) + (currentPage + 1));
            DbEntityHelper.derive(oldJob, epiJob);
            jobs.add(epiJob);
        }

        JSONArray list = data.getJSONArray("list");

        for (int i = 0; i < list.size(); i++) {
            JSONObject epiShow = list.getJSONObject(i);

            Show show = new Show();
            try {
                show.setName(epiShow.getString("t2"));
                show.setEpisode(epiShow.getInteger("t1"));
                show.setCode(epiShow.getString("video_id"));
            }catch (Exception ex){
                ex.printStackTrace();
            }
                show.setDepth(2);
                show.setPlatformId(oldJob.getPlatformId());
                show.setParentCode(oldJob.getCode());

            String isNew = epiShow.getString("isnew");// 暂时发现=2为预告片
            if (!"2".equals(isNew)) {
                shows.add(show);
            }
            /**
             * 芒果抓取vip信息
             * */
            //防止空指针
            if (Integer.valueOf(1).equals(epiShow.getInteger("isvip"))) {
                VipEpisode ve = new VipEpisode();
                ve.setPlatformId(show.getPlatformId());
                ve.setCode(show.getCode());
                putModel(page, ve);
            }

            Job job = new Job(START_STR + epiShow.getString("url"));
            DbEntityHelper.derive(oldJob, job);
            job.setCode(epiShow.getString("video_id"));
            jobs.add(job);

            Job commentJob = new Job(String.format(COMMENT_URL, epiShow.getString("video_id")));
            DbEntityHelper.derive(oldJob, commentJob);
            commentJob.setCode(epiShow.getString("video_id"));
            jobs.add(commentJob);

            // 弹幕
            Job barrageJob = MgExtend.barrageTextJob(epiShow.getString("video_id"),epiShow.getString("clip_id"));
            DbEntityHelper.derive(oldJob, barrageJob);
            barrageJob.setCode(epiShow.getString("video_id"));
            jobs.add(barrageJob);
        }
        putModel(page, jobs);
        putModel(page, shows);
    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.sites;
    }
}
