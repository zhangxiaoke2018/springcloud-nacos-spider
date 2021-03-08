package com.jinguduo.spider.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.repo.JobStateRedisRepo;

@Component
public class JobStateCache {

    // redis 开关
    @Value("${onespider.master.use-redis}")
    private boolean useRedis = false;

    @Autowired(required = false)
    private JobStateRedisRepo jobStateRedisRepo;
    
    public Long findJobState(Job job) {
        if (useRedis) {
            return jobStateRedisRepo.findJobStateByKey(job.getId());
        }
        return null;
    }

    public void saveJobState(Job job) {
        if (!useRedis) {
            return;
        }
        jobStateRedisRepo.saveJobState(job);
    }
}
