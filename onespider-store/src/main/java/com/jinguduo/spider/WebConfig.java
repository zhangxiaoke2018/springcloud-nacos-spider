package com.jinguduo.spider;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jinguduo.spider.common.msg.GzipDecompressFilter;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean gzipDecompressFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new GzipDecompressFilter());
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        registration.setOrder(0);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
