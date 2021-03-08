package com.jinguduo.spider.spider.tengxun;

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
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;

import java.util.List;

/**
 * 腾讯视频获取评论Id
 */
@Worker
@CommonsLog
public class TengxunCommentIdApiSpider extends CrawlSpider {

    final static String COMMENT_COUNT_URL = "http://coral.qq.com/article/%s/commentnum";

    //评论文本URL
    final static String COMMENT_TEXT_URL = "https://coral.qq.com/article/%s/comment?commentid=&reqnum=20";

    private Site site = SiteBuilder.builder().setDomain("ncgi.video.qq.com").build();

    private PageRule rule = PageRule.build()
            .add("/fcgi-bin/", page -> captureCommmentIdProcess(page));

    private void captureCommmentIdProcess(Page page) {


        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        Long commentId = null;
        JSONObject jsonObject = null;
        Job newJob = null;

        try {
            /** 解析得到评论Id */
            rawText = rawText.substring(rawText.indexOf("=") + 1).replace(";", "");
            jsonObject = JSONObject.parseObject(rawText);

            commentId = jsonObject.getLong("comment_id");

            List<Job> jobList = Lists.newArrayListWithCapacity(2);
            if (commentId != null && commentId != 0) {
                /** 生成评论数job */
                newJob = new Job(String.format(COMMENT_COUNT_URL, commentId));
                DbEntityHelper.derive(job, newJob);
                newJob.setCode(job.getCode());
                jobList.add(newJob);

                /**生成评论文本Job created by gsw 2017年2月22日12:57:55*/
                Job comment_content_job = new Job(String.format(COMMENT_TEXT_URL, commentId));
                DbEntityHelper.derive(job, comment_content_job);
                comment_content_job.setFrequency(FrequencyConstant.COMMENT_TEXT);
                comment_content_job.setCode(job.getCode());
                jobList.add(comment_content_job);
            } else if (commentId == null) {
                //commentId=0表示没有评论不打印错误日志
                log.warn("create comment count and text job fail,url:[" + job.getUrl() + "]");
            }

            putModel(page, jobList);
        } catch (Exception e) {
            throw new AntiSpiderException("ncgi.video.qq.com , create commentnum job error");
        }
    }


    @Override
    public PageRule getPageRule() {
        return rule;
    }

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }
}
