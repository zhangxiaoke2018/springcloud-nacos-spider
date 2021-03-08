package com.jinguduo.spider.cluster.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 分离出JobWrapper类，用于JobManager
 *
 */
@Data
public class JobWrapper implements Serializable, Comparable<JobWrapper> {

	private static final long serialVersionUID = -5182697172099483667L;
	
	private volatile Job job;
    private volatile Integer partitionKey; // 不唯一，可能重复
    private volatile String workerUuid;
    
    public JobWrapper() {
    }
    
    public JobWrapper(Job job) {
    	this.job = job;
    }
    
    public boolean isUpdated(Job job) {
    	return !this.job.equals(job);
    }

    @Override
    public int compareTo(JobWrapper o) {
        int r = 0;
        if (job.getFrequency() > o.job.getFrequency()) {
            r = 1;
        }
        return r;
    }
}
