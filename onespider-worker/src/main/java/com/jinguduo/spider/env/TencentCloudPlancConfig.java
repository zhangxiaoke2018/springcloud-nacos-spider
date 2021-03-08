package com.jinguduo.spider.env;

import com.jinguduo.spider.common.metric.MetricReportor;
import com.jinguduo.spider.common.metric.TencentCloudCustomMetricReportor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("planc")
public class TencentCloudPlancConfig {

    @Bean
    public MetricReportor metricReportor(RestTemplate restTemplate) {
        return new TencentCloudCustomMetricReportor(restTemplate);
    }

}
