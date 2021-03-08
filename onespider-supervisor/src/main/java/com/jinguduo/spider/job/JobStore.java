package com.jinguduo.spider.job;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.job.fetch.FetchJob;
@Component
public class JobStore implements InitializingBean {

    @Autowired(required = false)
    private List<FetchJob> fetchJobs;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        fetchJobs.forEach(f -> f.process());
    }
}
