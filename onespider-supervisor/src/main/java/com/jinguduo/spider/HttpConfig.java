package com.jinguduo.spider;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableList;

@Configuration
public class HttpConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        return restTemplate;
    }
    
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        RequestConfig requestConfig = RequestConfig
                .custom()
                // 从连接池中获取连接的超时时间，超过该时间未拿到可用连接， 会抛出
                //   org.apache.http.conn.ConnectionPoolTimeoutException:
                //     Timeout waiting for connection from pool
                .setConnectionRequestTimeout((int)TimeUnit.SECONDS.toMillis(5))
                // 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
                .setConnectTimeout((int)TimeUnit.SECONDS.toMillis(10))
                // 服务器返回数据(response)的时间，超过该时间抛出read timeout
                .setSocketTimeout((int)TimeUnit.SECONDS.toMillis(60))
                .build();

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(true)
                .setTcpNoDelay(false)
                .setSoReuseAddress(true)
                .setSoTimeout((int)TimeUnit.SECONDS.toMillis(60))
                .setSndBufSize(32768)  // 32K
                .build();
        
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(50);
        cm.setDefaultMaxPerRoute(20);
        cm.setDefaultSocketConfig(socketConfig);
        cm.closeExpiredConnections();
        cm.closeIdleConnections(2, TimeUnit.SECONDS);

        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .setConnectionManager(cm)
                .setDefaultHeaders(ImmutableList.of(new BasicHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE)))
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }
}
