package com.jinguduo.spider.common.metric;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableMap;

public class SimpleMetric implements Metrizable {

    private static final long serialVersionUID = 7094050247721338695L;
    
    private final String namespace;

    private final String metricName;
    
    private final int type;
    
    private final AtomicInteger counter;
    
    private final Map<String, String> dimensions;

	public SimpleMetric(String namespace, String metricName, int type, Map<String, String> dimensions) {
    	this.namespace = namespace;
        this.metricName = metricName;
        this.type = type;
        this.dimensions = ImmutableMap.copyOf(dimensions);
        this.counter = new AtomicInteger(0);
    }
    
    @Override
    public int addAndGet(final int val) {
        return counter.addAndGet(val);
    }
    
    @Override
    public int getAndSet(final int val) {
        return counter.getAndSet(val);
    }

    @Override
    public MetricBean getMetricBean() {
        return MetricBean.builder()
	        	.namespace(namespace)
	            .metricName(metricName)
	            .type(type)
	            .dimensions(dimensions)
	            .timestamp(System.currentTimeMillis())
	            .value(counter.getAndSet(0))
	            .build();
    }
}
