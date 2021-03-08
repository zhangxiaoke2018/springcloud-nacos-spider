package com.jinguduo.spider.job.fetch;

import com.jinguduo.spider.access.OneAccessor;
import com.jinguduo.spider.cluster.model.Job;
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
 * @DATE 15/10/2020
 */
@Component
@CommonsLog
@RefreshScope
public class FetchJobFromNews implements FetchJob {

    @Value("${onespider.store.host.job_news_task}")
    private String jobTaskHost;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private OneAccessor oneAccessor;

    private final Timer timer = new Timer("FetchJobFromNewsTimer");

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
        BAIDU_PATH("/baidu", 4);

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
