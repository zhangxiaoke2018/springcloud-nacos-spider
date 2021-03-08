package com.jinguduo.spider.env;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.common.metric.MetricReportor;
import com.jinguduo.spider.common.metric.TencentCloudCustomMetricReportor;

@Configuration
@Profile("planb")
public class TencentCloudConfig {

    @Bean
    public MetricReportor metricReportor(RestTemplate restTemplate) {
        return new TencentCloudCustomMetricReportor(restTemplate);
    }

}
