package com.jinguduo.spider.env;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.jinguduo.spider.common.metric.FakeMetricReportor;
import com.jinguduo.spider.common.metric.MetricReportor;

@Configuration
@Profile({"stage", "dev"})
public class StageConfig {

    @Bean
    public MetricReportor metricReportor() {
        return new FakeMetricReportor();
    }

}
