package com.jinguduo.spider.env;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.jinguduo.spider.common.metric.AliyunCustomMetricReportor;
import com.jinguduo.spider.common.metric.MetricReportor;

@Configuration
@Profile("prod")
public class AliyunConfig {

    @Bean
    public MetricReportor metricReportor() {
        return new AliyunCustomMetricReportor();
    }

}
