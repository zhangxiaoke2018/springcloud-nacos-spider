package com.jinguduo.spider.common.type;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于时间期间的频率配额
 * <p>
 *   在预设的时间单位期间（duration），对超过限制频率（limit）的调用返回False。<br>
 *   <b>返回False的同时，此次调用不记录</b>
 *
 */
public class TimedRateQuota implements Quota {
	
	private Map<String, TimestampRing> table = new ConcurrentHashMap<>();
	
	private final int limit;
	private final long peroid;

	public TimedRateQuota(long duration, TimeUnit timeUnit, int limit) {
		this.limit = limit;
		this.peroid = timeUnit.toMillis(duration);
	}
	
	@Override
	public boolean isAboved(String key) {
		TimestampRing ring = table.get(key);
		if (ring == null) {
		    synchronized(this) {
		        ring = table.get(key);
		        if (ring == null) {
		            ring = new TimestampRing(this.limit);
		            table.put(key, ring);
                }
		    }
		}
		return ring.isAboved();
	}
	
	@Override
	public void reset(String key) {
	    TimestampRing ring = table.get(key);
	    if (ring != null) {
            ring.clear();
        }
	}
	
	class TimestampRing {
		final long[] ring;
		volatile int point = 0;
		final int limit;
		
		TimestampRing(final int limit) {
			ring = new long[limit + 1];
			this.limit = limit;
		}
		
		synchronized void clear() {
		    point = 0;
		    for (int i = 0; i < ring.length; i++) {
                ring[i] = 0; 
            }
		}
		
		synchronized boolean isAboved() {
			int j = 0;
			long t = System.currentTimeMillis() - peroid;
			for (int i = 0; i < ring.length; i++) {
				if (ring[i] > t) {
					j++;
				}
			}
			boolean aboved = (j >= limit);
			
			// touch if not aboved
			if (!aboved) {
			    point = point < ring.length ? point : 0;
	            ring[point++] = System.currentTimeMillis();
	        }
			return aboved;
		}
	}
}
