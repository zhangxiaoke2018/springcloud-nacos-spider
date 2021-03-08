package com.jinguduo.spider.spider.sohu;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

import java.util.List;

@Worker
@CommonsLog
public class SohuDramaSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("pl.hd.sohu.com")
            .setCharset("GBK")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    final static String URL_DRAMA_PLAY_COUNT = "http://count.vrs.sohu.com/count/queryext.action?vids=%s&plids=%s&callback=playCountVrs";

    /**
     * 评论url
     */
    //private final static String COMMENT_COUNT_URL = "http://changyan.sohu.com/api/2/topic/load?client_id=%s&topic_url=%s&topic_source_id=%s";
    /**
     * 新的评论接口
     */
    private final static String COMMENT_COUNT_NEW_URL = "https://api.my.tv.sohu.com/comment/api/v1/count?vrs_vids=%s";
    private final static String CLIENT_ID = "cyqyBluaj";// 固定常数

    private final static String DANMU_URL_1 = "http://api.danmu.tv.sohu.com/danmu?act=dmlist_v2&vid=%s&page=1&pct=2&request_from=sohu_vrs_player&o=4&aid=%s";
    private final static String DANMU_URL_2 = "http://api.danmu.tv.sohu.com/danmu?act=dmlist_v2&vid=%s&page=1&pct=2&request_from=sohu_vrs_player&o=1&aid=%s";

    private PageRule rules = PageRule.build()
            .add("playlistid", page -> list(page))
            .add("queryext.action", page -> playCount(page));

    private void list(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(page.getRawText().substring(page.getRawText().indexOf("(") + 1, page.getRawText().lastIndexOf(")")));

        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();

        String playlistid = json.getString("playlistid");
        JSONArray videos = json.getJSONArray("videos");

        JSONObject video = null;

        for (int i = 0; i < videos.size(); i++) {

            video = videos.getObject(i, JSONObject.class);

            String vid = video.getString("vid");
            if (StringUtils.isBlank(vid)) {
                log.error("The vid maybe null: " + job.getUrl());
                continue;
            }
            Integer isPay = video.getInteger("ispay");
            if (isPay == null) {
                isPay = video.getInteger("tvIsFee");
            }
            if (isPay != null && isPay == 1) {
                // vip标志
                VipEpisode vip = new VipEpisode();
                vip.setCode(vid);
                vip.setPlatformId(job.getPlatformId());
                putModel(page, vip);
            }
            String pageUrl = video.getString("pageUrl");
            Show show = new Show(video.getString("name"), vid, job.getPlatformId(), job.getShowId());
            show.setDepth(2);
            show.setUrl(pageUrl);
            Integer showDate = null;
            if (StringUtils.equals(job.getCode(), "9337023")) {//极速前进第四季
                showDate = video.getInteger("showDate");//有些综艺会走到这里逻辑，应该取showDate做分集
            }
            show.setEpisode((showDate != null && showDate != 0) ? showDate : Integer.valueOf(video.getString("order")));
            show.setParentCode(job.getCode());
            shows.add(show);

            String purl = String.format(URL_DRAMA_PLAY_COUNT, video.getString("vid"), playlistid);
            cPlaycountJob(jobs, job, vid, purl);

            //  String curl = String.format(COMMENT_COUNT_URL, CLIENT_ID, video.getString("pageUrl"), vid);
            String curl = String.format(COMMENT_COUNT_NEW_URL, vid);
            cCommentJob(jobs, job, vid, curl);

            String durl = String.format(DANMU_URL_1, vid, playlistid) + "&t=" + String.valueOf(System.currentTimeMillis());
            String durl2 = String.format(DANMU_URL_2, vid, playlistid) + "&t=" + String.valueOf(System.currentTimeMillis());
            cDanmuJob(jobs, job, vid, durl, durl2);
        }
        putModel(page, shows);
        putModel(page, jobs);
    }

    /**
     * create capture playCount jobs
     */
    private void cPlaycountJob(List<Job> jobs, Job job, String vid, String purl) {
        Job newJob = new Job(purl);
        DbEntityHelper.derive(job, newJob);
        newJob.setCode(vid);

        jobs.add(newJob);
    }

    /**
     * create capture commentCount jobs
     */
    private void cCommentJob(List<Job> jobs, Job job, String vid, String curl) {
        Job commentJob = new Job(curl);
        DbEntityHelper.derive(job, commentJob);
        commentJob.setCode(vid);
        commentJob.setFrequency(FrequencyConstant.COMMENT_COUNT);

        jobs.add(commentJob);
    }

    //http://api.danmu.tv.sohu.com/danmu?act=dmlist_v2&vid=3488552&page=1&pct=2&request_from=sohu_vrs_player&o=4&aid=9141936
    private void cDanmuJob(List<Job> jobs, Job oldJob, String vid, String durl, String durl2) {
        Job newJob = DbEntityHelper.deriveNewJob(oldJob, durl);
        newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        newJob.setCode(vid);
        jobs.add(newJob);

        Job newJob2 = DbEntityHelper.deriveNewJob(oldJob, durl2);
        newJob2.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        newJob2.setCode(vid);
        jobs.add(newJob2);
    }

    private void playCount(Page page) {
        String text = page.getRawText();
        Long playCount = NumberHelper.parseLong(text.substring(text.lastIndexOf("total") + 7, text.lastIndexOf(",")), -1);
        Job job = ((DelayRequest) page.getRequest()).getJob();
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(job, showLog);
        showLog.setPlayCount(playCount);

        putModel(page, showLog);
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
