package com.guduo.dashboard.config;

import java.nio.charset.Charset;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableList;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/17 上午10:32
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
    	RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
            restTemplate.setMessageConverters(ImmutableList.of(
                new StringHttpMessageConverter(Charset.forName("UTF-8")),
                new FormHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter()
            ));
        return restTemplate;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 5000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        cm.setDefaultMaxPerRoute(200);
        cm.setMaxTotal(200);

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        return httpClient;
    }
}
