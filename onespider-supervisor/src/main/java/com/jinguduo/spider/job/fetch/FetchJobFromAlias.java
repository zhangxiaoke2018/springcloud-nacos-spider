package com.jinguduo.spider.job.fetch;

import com.jinguduo.spider.access.AccessorClient;
import com.jinguduo.spider.access.OneAccessor;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.job.JobManager;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 28/07/2017 15:33
 */
@Component
@CommonsLog
@RefreshScope
public class FetchJobFromAlias implements FetchJob {

    @Value("${onespider.store.host.job_task}")
    private String jobTaskHost;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private OneAccessor oneAccessor;

    private final Timer timer = new Timer("FetchJobFromAliasTimer");

    private long delay = 15 * 1000;

    @Override
    public void process() {

        for (PathEnum pathEnum : PathEnum.values()) {

            Integer period = pathEnum.getPeriod() * 60 * 60 * 1000;

            timer.scheduleAtFixedRate(new Task(pathEnum.getPath(jobTaskHost)), delay, period);
        }
    }

    class Task extends TimerTask {

        private final String url;

        Task(String url){
            this.url = url;
        }
        @Override
        public void run() {
            try {
                List<Job> jobs = oneAccessor.fetchJobs(url);
                jobs.forEach(j -> {
//                    j.setFrequency(FrequencyConstant.BLANK);  //  Job频率由SpiderSettings决定
                    jobManager.add(j);
                    });
                log.info("url:" + url + " , get_job_size: " + jobs.size());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    enum PathEnum {

        INDEX_360_PATH("/index360", 4),
        MEDIA_360_PATH("/media360", 4),
        CUSTOMER_360_PATH("/customer360", 12),
        NEWS_TOUTIAO_PATH("/toutiao", 4),
        WEIBO_INDEX_PATH("/weiboIndex", 2),
        //BAIDU_VIDEO_PATH("/baiduVideo", 4),
        NEWS_360_PATH("/news360", 4),
        BAIDU_TIEBA_PATH("/baiduTieba", 4),
        BAIDU_NEWS_PATH("/baiduNews", 4),
        SOUGOUWECHAT_SEARCH_PATH("/sougouWechatSearch", 4),
        WEIBO_SEARCH_PATH("/weiboSearch", 4),
        WECHAT_ARTICLE("/wechatArticle", 1),
        //BILIBILI_SEARCH_PATH("/bilibiliSearch", 4),
        CORE_KEYWORD_PATH("/coreKeyword", 4),
        NEW_BAIDU_NEWS("/newBaiduNews",4);

        private final String path;

        // 间隔多久执行一次任务, 单位：小时
        private final Integer period;

        PathEnum(String path, Integer period) {
            this.path = path;
            this.period = period;
        }

        public String getPath(String basePath) {
            return basePath + path;
        }

        public Integer getPeriod() {
            return period;
        }
    }
}
