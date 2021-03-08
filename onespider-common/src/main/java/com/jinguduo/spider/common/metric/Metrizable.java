package com.jinguduo.spider.common.metric;

import java.io.Serializable;

public interface Metrizable extends Serializable {

    int addAndGet(int i);
    
    int getAndSet(int i);

    MetricBean getMetricBean();
}
