package com.jinguduo.spider.spider.tengxun;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

/**
 * Created by csonezp on 2016/11/23.
 */
@Worker
@Slf4j
public class TengxunDanmuIdApiSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("bullet.video.qq.com")
            .addHeader("cache-control", "no-cache")
            .build();

    private final static String DANMU_COUNT_URL = "https://mfm.video.qq.com/danmu?otype=json&target_id=%s&session_key=0%%2C0%%2C0&_=1479627541687";

    // 新增:&count=80&second_count=6 参数
    private final static String DANMU_TEXT_URL = "https://mfm.video.qq.com/danmu?otype=json&timestamp=15&target_id=%s&count=10000&second_count=10000&session_key=%s";

    private PageRule rule = PageRule.build()
            .add("", page -> getTargetId(page));

    /***
     * 腾讯视频，需要先获取targetId，才能获取弹幕内容
     * @param page
     */
    private void getTargetId(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        List<Job> jobs = Lists.newArrayList();

        Html html = page.getHtml();

        String targetId = RegexUtil.getDataByRegex(html.toString(), "targetid=(.*?)&amp");

        if (StringUtils.isNotBlank(targetId)){
            cCountJob(jobs,oldJob,targetId);
            cContentJob(jobs,oldJob,targetId);
            if (CollectionUtils.isNotEmpty(jobs)){
                putModel(page,jobs);
            }
        }
    }

    /***
     * 创建弹幕文本任务
     * @param jobs
     * @param oldJob
     * @param targetId
     */
    private void cContentJob(List<Job> jobs, Job oldJob, String targetId) {
        try {
            String url = String.format(DANMU_TEXT_URL, targetId, "0%2C0%2C0") + "&_=" + System.currentTimeMillis();
            Job newJob = DbEntityHelper.deriveNewJob(oldJob,url);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /***
     * 创建弹幕数任务
     * @param jobs
     * @param oldJob
     * @param targetId
     */
    private void cCountJob(List<Job> jobs, Job oldJob, String targetId) {
        String targetUrl = String.format(DANMU_COUNT_URL, targetId);
        Job newJob;
            newJob = new Job(targetUrl);
            DbEntityHelper.derive(oldJob, newJob);
            newJob.setCode(oldJob.getCode());
            jobs.add(newJob);
    }


    @Override
    public PageRule getPageRule() {
        return rule;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
