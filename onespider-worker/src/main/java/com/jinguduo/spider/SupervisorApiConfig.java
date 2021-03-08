package com.jinguduo.spider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.CollectionStorePipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequestScheduler;
import com.jinguduo.spider.cluster.scheduler.DistributedScheduler;
import com.jinguduo.spider.cluster.worker.Heartbeat;
import com.jinguduo.spider.cluster.worker.HttpHeartbeat;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;

@Configuration
public class SupervisorApiConfig {

    @Value("${onespider.master.job.url}")
    private String jobsStoreUrl;

    @Value("${onespider.master.jobs.sync.url}")
    private String jobSyncUrl;

    @Value("${onespider.master.worker.heartbeat.url}")
    private String heartbeatUrl;

    @Bean
    public Pipeline jobStorePipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new CollectionStorePipeline(simpleHttp, Job.class,  jobsStoreUrl);

        return pipeline;
    }
    
    @Bean
    public Heartbeat httpHeartbeat(RestTemplate restTemplate) {
        Heartbeat httpHeartbeat = new HttpHeartbeat(restTemplate, heartbeatUrl);

        return httpHeartbeat;
    }
    
    @Bean
    public DistributedScheduler delayRequestScheduler(RestTemplate restTemplate) {
        DelayRequestScheduler delayRequestScheduler = new DelayRequestScheduler();

        delayRequestScheduler.setJobUrl(jobSyncUrl);
        delayRequestScheduler.setRestTemplate(restTemplate);

        return delayRequestScheduler;
    }
}
