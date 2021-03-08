package com.jinguduo.spider.cluster.scheduler;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.webmagic.Request;

public class DelayRequest extends Request implements Delayed {

	private static final long serialVersionUID = -7434704903492587855L;
	
	private Job job;
	private long startTime;
	
	private DelayRequest(String url, String method, int delay) {
		super(url);
		setMethod(method);
		setDelay(delay);
	}
	
	public DelayRequest(Job job) {
        this(job, job.getFrequency());
    }
	
	public DelayRequest(Job job, int delay) {
        this(job.getUrl(), job.getMethod(), delay);
        this.setRequestBody(job.getHttpRequestBody());
        this.job = job;
    }
	
	public void setDelay(int delay) {
		this.startTime = System.currentTimeMillis() + (delay * 1000);
	}
	
	public DelayRequest resetStartTime() {
		this.startTime = this.startTime + (job.getFrequency() * 1000);
	    return this;
	}

	@Override
	public int compareTo(Delayed o) {
		int r = 0;
		if (this.startTime < ((DelayRequest) o).startTime) {
			r = -1;
		} else if (this.startTime > ((DelayRequest) o).startTime) {
			r = 1;
		}
		return r;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		long diff = startTime - System.currentTimeMillis();
		return unit.convert(diff, TimeUnit.MILLISECONDS);
	}

	public Job getJob() {
		return job;
	}

	public long getStartTime() {
		return startTime;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((job == null) ? 0 : job.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof DelayRequest))
            return false;
        DelayRequest other = (DelayRequest) obj;
        if (job == null) {
            if (other.job != null)
                return false;
        } else if (!job.equals(other.job))
            return false;
        return true;
    }
}
