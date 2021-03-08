package com.jinguduo.spider.spider.manmanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lc on 2019/5/20
 * https://m.manmanapp.com/works/comic-list-ajax.html
 */
@Worker
@Slf4j
public class ComicMmmhPcSpider extends CrawlSpider {


    private static String REQUEST_URL = "https://m.manmanapp.com/works/comic-list-ajax.html?%s";

    private Site site = SiteBuilder.builder()
            .setDomain("m.manmanapp.com")
            .addSpiderListener(new ComicMmmhPcDownLoaderListener())
            .build();


    private PageRule rules = PageRule.build()
            .add("comic-list-ajax", page -> analyze(page));

    private void analyze(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        Integer status = jsonObject.getInteger("code");
        if (null == status || status != 1) {
          //  log.error("m.manmanapp.com query fail ,this code is ->{} and result is ->{} ,and url is ->{}", job.getCode(), page.getRawText(),job.getUrl());
            return;
        }
        String httpBody = StringUtils.substring(url, StringUtils.indexOf(url, "?") + 1);

        JSONArray datas = jsonObject.getJSONArray("data");
        Integer pageSize = datas.size();
        String pageNum = StringUtils.substring(httpBody, StringUtils.indexOf(httpBody, "page=") + 5, httpBody.length());
        int beforeNumber = (Integer.valueOf(pageNum) - 1) * pageSize;

        List<ComicEpisodeInfo> episodeList = new ArrayList<>();
        Date day = DateUtil.getDayStartTime(new Date());

        for (int i = 0; i < pageSize; i++) {
            try {
                JSONObject data = (JSONObject) datas.get(i);
                String episodeNo = data.getString("id");
                String title = data.getString("title");
                Date onLineDate = DateUtils.parseDate(data.getString("publish_time"), "yyyy-MM-dd");
                String likesStr = data.getString("likes");
                Integer likes = StringUtils.isEmpty(likesStr) ? 0 : Integer.valueOf(likesStr);
                Integer is_read = data.getInteger("is_read");
                Integer vipStatus = is_read == 0 ? 1 : 0;
                Integer episodeNumber = beforeNumber + i + 1;

                ComicEpisodeInfo info = new ComicEpisodeInfo();
                info.setCode(job.getCode());
                info.setPlatformId(35);
                info.setDay(day);
                info.setName(title);
                info.setEpisode(episodeNumber);
                info.setVipStatus(vipStatus);
                info.setComicCreatedTime(onLineDate);
                info.setChapterId(episodeNo);
                info.setLikeCount(likes);
                episodeList.add(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       // log.info("m.manmanapp.com query success ,this code is ->{} and result is ->{}", job.getCode(), episodeList);

        putModel(page, episodeList);
        //进入这个位置，代表页数没有翻完，开始翻页。每页返回固定10条，无法更改
        int end = StringUtils.lastIndexOf(httpBody, "&");
        String newPageBody = StringUtils.substring(httpBody, 0, end) + "&page=" + (Integer.valueOf(pageNum) + 1);
        Job pageJob = new Job(String.format(REQUEST_URL, newPageBody));
        DbEntityHelper.derive(job, pageJob);
        pageJob.setMethod("POST");
        pageJob.setCode(job.getCode());
        pageJob.setKind(JobKind.Once);
        putModel(page, pageJob);

       // log.info("m.manmanapp.com page change ,old is ->{} and new is ->{}", httpBody, newPageBody);
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
