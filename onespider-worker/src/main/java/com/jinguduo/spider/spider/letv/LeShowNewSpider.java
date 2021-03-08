package com.jinguduo.spider.spider.letv;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.CommonsLog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;

import org.apache.commons.lang3.StringUtils;

/**
 * 旧的获取分集列表的爬虫已经失效，现从列表获取分集任务一集的vid再通过vid获取到其他的分集列表(暂时)
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年5月16日 下午3:39:46
 *
 */
@Worker
@CommonsLog
public class LeShowNewSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("d.api.m.le.com").build();

    private final static String ITEM_COUNT_URL = "http://v.stat.letv.com/vplay/queryMmsTotalPCount?vid=%s";

    private final static String BARRAGE_URL = "http://cdn.api.my.letv.com/danmu/list?vid=%s&cid=%s&start=0&getcount=1";

    //评论文本
    private final static String COMMENT_CONTENT_URL = "http://api.my.le.com/vcm/api/list?rows=20&page=1&listType=1&xid=%s&pid=%s";

    PageRule rule = PageRule.build().add("/card/", page -> listProcess(page))//剧分集
            .add("/detail", page -> zongYiProcess(page));//综艺分集

    /***
     * 解析分集list
     * 
     * @param page
     */
    private void listProcess(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String url = page.getRequest().getUrl();

        String pid = null, cid = null, epiStr = null;
        Integer epiInt = null;

        log.debug("begin leTv videoItems show and job process , url : " + url);

        /** 提取专辑Id */
        Pattern patternP = Pattern.compile("id=(\\d*)&cid=(\\d*)", Pattern.DOTALL);
        Matcher matcherP = patternP.matcher(url);
        if (matcherP.find()) {
            pid = matcherP.group(1);
            cid = matcherP.group(2);
        } else {
            return;
        }

        /** 解析json */
        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        JSONArray videoList=null;
        try{
            videoList = jsonObject.getJSONObject("data").getJSONObject("episode").getJSONArray("videolist");
        }catch(Exception e){
            log.error("catch a error from url:"+oldJob.getUrl()+" errMsg:"+e.getMessage());
            return;
        }
        if (null == videoList || videoList.isEmpty()) {
            log.warn(" return epi jsondata is null ,data:" + videoList + "");
            return;
        }
        List<Job> jobs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();

        for (int i = 0; i < videoList.size(); i++) {
            JSONObject d = videoList.getObject(i, JSONObject.class);

            String vid = d.getString("vid");
            Integer ispay = d.getInteger("ispay");
            if(ispay!=null&&ispay==1){
                // vip标志
                VipEpisode vip = new VipEpisode();
                vip.setCode(vid);
                vip.setPlatformId(oldJob.getPlatformId());
                putModel(page, vip);
            }
            String itemCountUrl = String.format(ITEM_COUNT_URL, vid);
            String danmuUrl = String.format(BARRAGE_URL, vid, cid);

            itemCountJob(jobs, oldJob, vid, itemCountUrl);
            barrageJob(jobs, oldJob, vid, danmuUrl);

            createCommentContentJob(page, oldJob, vid, pid);

            /**
             * 判断分集,排除预告片和花絮 "videoType":"180001" 180003,182267 ：预告片,花絮,片头片尾
             */
            if ("180001".equals(d.getString("videoType"))) {// 正片
                Show show = new Show(d.getString("title"), d.getString("vid"), oldJob.getPlatformId(),
                        oldJob.getShowId());
                show.setDepth(2);
                show.setParentCode(oldJob.getCode());

                epiStr = d.getString("episode");
                try {
                    if (!NumberHelper.isNumeric(epiStr)){
                        epiInt = 0;
                        log.warn("le show spider get epiStr is not a Int number from url [" + url + "] , epiStr: ["+epiStr+"]");
                    } else {
                        epiInt = Integer.valueOf(epiStr);
                    }
                    show.setEpisode(epiInt);
                } catch (Exception e) {
                    log.error("le show spider catch a exception from url [" + url + "],errMsg:" + e.getMessage(), e);
                }
                shows.add(show);
            }

        }
        putModel(page,jobs);
        putModel(page,shows);
    }

    //综艺process
    private void zongYiProcess(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String url = page.getRequest().getUrl();
        log.debug("begin le zongyi list process , url : " + url);

        /** 解析json */
        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        JSONArray videoList=null;
        try{
            videoList = jsonObject.getJSONObject("data").getJSONArray("list");
        }catch(Exception e){
            log.error("catch a error from url:"+oldJob.getUrl()+" errMsg:"+e.getMessage());
            return;
        }
        
        if (null == videoList || videoList.isEmpty()) {
            log.warn(" return epi jsondata is null ,data:" + videoList + "");
            return;
        }
        List<Job> jobs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();

        for (int i = 0; i < videoList.size(); i++) {
            JSONObject d = videoList.getObject(i, JSONObject.class);

            String vid = d.getString("vid");
            String cid = d.getString("cid");
            String pid = d.getString("pid");
            Integer ispay = d.getInteger("ispay");
            if(ispay!=null&&ispay==1){
                // vip标志
                VipEpisode vip = new VipEpisode();
                vip.setCode(vid);
                vip.setPlatformId(oldJob.getPlatformId());
                putModel(page, vip);
            }

            String itemCountUrl = String.format(ITEM_COUNT_URL, vid);
            String barrageUrl = String.format(BARRAGE_URL, vid, cid);

            //此处已经可以拿到分集播放量
            itemCountJob(jobs, oldJob, vid, itemCountUrl);
            barrageJob(jobs, oldJob, vid, barrageUrl);

            createCommentContentJob(page, oldJob, vid, pid);

            /**
             * 判断分集,排除预告片和花絮 "videoType":"180001" 180003,182267 ：预告片,花絮,片头片尾
             */
            JSONObject jsonObject2 = d.getJSONObject("video_type");
            if (jsonObject2.containsKey("180001")) {// 正片
                Integer epiInt = 0;
                Show show = new Show(d.getString("sub_title"), d.getString("vid"), oldJob.getPlatformId(),
                        oldJob.getShowId());
                show.setDepth(2);
                show.setParentCode(oldJob.getCode());

                String epiStr = d.getString("episode");
                try {
                    if (!NumberHelper.isNumeric(epiStr)){
                        epiInt = 0;
                        log.warn("le show spider get epiStr is not a Int number from url [" + url + "] , epiStr: ["+epiStr+"]");
                    } else {
                        epiInt = Integer.valueOf(epiStr);
                    }
                    show.setEpisode(epiInt);
                } catch (Exception e) {
                    log.error("le show spider catch a exception from url [" + url + "],errMsg:" + e.getMessage(), e);
                }
                shows.add(show);
            }

        }
        putModel(page,jobs);
        putModel(page,shows);
    }

    private void itemCountJob(List<Job> jobs, Job oldJob, String vid, String playCountUrl) {
        try {
            Job newJob = DbEntityHelper.deriveNewJob(oldJob, playCountUrl);
            newJob.setCode(vid);
            newJob.setFrequency(FrequencyConstant.GENERAL_PLAY_COUNT);
            jobs.add(newJob);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void barrageJob(List<Job> jobs, Job oldJob, String vid, String url) {
        try {
            Job job = DbEntityHelper.deriveNewJob(oldJob, url);
            job.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            job.setCode(vid);
            jobs.add(job);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void createCommentContentJob(Page page,Job oldJob, String vid, String pid) {
        if(StringUtils.isNotBlank(pid) && StringUtils.isNotBlank(vid)) {
            try {
                Job commentComtentJob = DbEntityHelper.deriveNewJob(oldJob, String.format(COMMENT_CONTENT_URL,vid,pid));
                commentComtentJob.setCode(vid);
                commentComtentJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
                putModel(page,commentComtentJob);
            }catch (Exception e){
                log.error("create leTv comment content job failed,cause pid or vied is null :pid:["+pid+"];cid:["+vid+"]");
            }
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }
}
