package com.guduo.dashboard;

import org.apache.http.client.fluent.Request;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/20 上午11:31
 */
@Configuration
public class MockConfig {

    @Bean
    public RestTemplate restTemplate() {
        return Mockito.mock(RestTemplate.class);
    }
    @Bean
    public Request request(){
        return Mockito.mock(Request.class);
    }
}
