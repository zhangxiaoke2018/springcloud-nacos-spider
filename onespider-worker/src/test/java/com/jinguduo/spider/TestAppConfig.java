package com.jinguduo.spider;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.cluster.spider.DefaultSpiderSettingLoader;
import com.jinguduo.spider.cluster.spider.SpiderSettingLoader;
import com.jinguduo.spider.common.metric.MetricReportor;
import com.jinguduo.spider.common.metric.FakeMetricReportor;
import com.jinguduo.spider.data.loader.ProxyStoreLoader;

@Configuration
@Profile("test")
public class TestAppConfig {

	@Bean(name = "spiderSettingLoader")
	@Primary
	public SpiderSettingLoader spiderSettingLoader() {
		return new DefaultSpiderSettingLoader();
	}
	
	@Bean
	@Primary
    public WorkerRunner workerRunner() {
        return Mockito.mock(WorkerRunner.class);
    }

    @Bean(name = "httpProxyStoreLoader")
    @Primary
    public ProxyStoreLoader proxyStoreLoader(RestTemplate restTemplate) {
        //return new ProxyStoreLoader(restTemplate, null);
        return Mockito.mock(ProxyStoreLoader.class);
    }
    
    @Bean
    public MetricReportor metricReportor() {
        return new FakeMetricReportor();
    }
}
