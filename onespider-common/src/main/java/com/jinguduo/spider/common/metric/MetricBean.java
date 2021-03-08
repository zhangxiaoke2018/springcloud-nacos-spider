package com.jinguduo.spider.common.metric;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricBean {

	private final String namespace;
    private final String metricName;
    private final int type;
    private final int value;
    private final Map<String, String> dimensions;
    private long timestamp;
}
