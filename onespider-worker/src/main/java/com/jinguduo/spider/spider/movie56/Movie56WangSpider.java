package com.jinguduo.spider.spider.movie56;


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Selectable;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/7/15 下午2:24
 */
@Worker
@CommonsLog
public class Movie56WangSpider extends CrawlSpider {

    private static final String PC_URL = "http://vstat.v.blog.sohu.com/dostat.do?method=getVideoPlayCount&v=%s";

    private static final String EX_PC_URL = "http://vstat.v.blog.sohu.com/vv/?id=%s";

    final static String COMMENT_URL = "http://comment.56.com/trickle/api/commentApi.php?a=flvLatest&vid=%s&pct=1&page=1&limit=1";//vid = MTM3ODg4Mzgy

    private Site site = SiteBuilder.builder().setDomain("www.56.com").build();

    private PageRule rules = PageRule.build()
            .add("/u\\d*/",page -> processNetMovie(page));//网大逻辑


    private void processNetMovie(Page page){

        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job == null) return;

        //第一种：code 是字母 process
        if(!NumberUtils.isNumber(job.getCode())){
            processExNetMovie(page,job);//<code>MTM1ODcyNDAw</code>
            return;
        }

        //任务集合
        List<Job> jobList = Lists.newArrayListWithCapacity(3);

        //第二种：code 是数字
            Job newJob = new Job(String.format(PC_URL, job.getCode()));
            DbEntityHelper.derive(job, newJob);
            newJob.setCode(job.getCode());
            jobList.add(newJob);

        Selectable url = page.getUrl();
        String code = url.regex("\\/\\w_(\\w*).html",1).get();

        if(StringUtils.isBlank(code)){
            log.error("get code by url fail ,url:["+url.get()+"]");
            return;
        }

            Job newJob2 = new Job(String.format(PC_URL, code));
            DbEntityHelper.derive(job, newJob2);
            newJob2.setCode(job.getCode());
            jobList.add(newJob2);

        //生成评论量job
            Job newCommentJob = new Job(String.format(COMMENT_URL, code));
            DbEntityHelper.derive(job, newCommentJob);
            jobList.add(newCommentJob);

        if(!jobList.isEmpty()){
            putModel(page,jobList);
        }
    }

    private void processExNetMovie(Page page,Job job){
        List<Job> jobList = Lists.newArrayListWithCapacity(2);
        //生成播放量job
            Job newJob = new Job(String.format(EX_PC_URL, job.getCode()));
            DbEntityHelper.derive(job, newJob);
            newJob.setCode(job.getCode());
            jobList.add(newJob);
        //生成评论量job
            Job newCommentJob = new Job(String.format(COMMENT_URL, job.getCode()));
            DbEntityHelper.derive(job, newCommentJob);
            newCommentJob.setCode(job.getCode());
            newCommentJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
            jobList.add(newCommentJob);
        if(!jobList.isEmpty()){
            putModel(page,jobList);
        }
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
