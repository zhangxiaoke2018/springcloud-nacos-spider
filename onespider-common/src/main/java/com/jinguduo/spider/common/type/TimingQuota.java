package com.jinguduo.spider.common.type;

import java.util.concurrent.TimeUnit;

public class TimingQuota implements Quota {
	
	private volatile long epoch;
	private final long period;
	
	public TimingQuota(long period, TimeUnit timeUnit) {
		this.epoch = System.currentTimeMillis();
		this.period = timeUnit.toMillis(period);
	}

	@Override
	public boolean isAboved(String key) {
		return (System.currentTimeMillis() - period) >= epoch;
	}

	@Override
	public void reset(String key) {
		this.epoch= System.currentTimeMillis();
	}

}
