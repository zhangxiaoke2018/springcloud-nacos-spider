package com.jinguduo.spider.env;

import com.jinguduo.spider.common.metric.FakeMetricReportor;
import com.jinguduo.spider.common.metric.MetricReportor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("planc")
public class TencentCloudPlancConfig {

    @Bean
    public MetricReportor metricReportor() {
        return new FakeMetricReportor();
    }
}
