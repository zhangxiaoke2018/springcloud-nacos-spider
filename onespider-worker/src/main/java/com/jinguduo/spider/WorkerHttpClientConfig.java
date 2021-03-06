package com.jinguduo.spider;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableList;
import com.jinguduo.spider.cluster.downloader.DownloaderManager;
import com.jinguduo.spider.cluster.downloader.HttpClientConfig;
import com.jinguduo.spider.cluster.downloader.HttpClientGenerator;
import com.jinguduo.spider.cluster.downloader.ImprovedDownloader;
import com.jinguduo.spider.common.proxy.FastProxyPool;
import com.jinguduo.spider.common.proxy.ProxyPoolManager;
import com.jinguduo.spider.data.loader.ProxyStoreLoader;

@Configuration
public class WorkerHttpClientConfig {
	
	@Value("${onespider.http.max-connection-per-route}")
	private int maxConnectionPerRoute;
	
	@Value("${onespider.http.max-connection}")
	private int maxConnection;
	
	@Value("${onespider.http.max-idle-time}")
	private int maxIdleTime;  // milliseconds
	
	@Value("${onespider.http.time-to-live}")
	private int timeToLive;  // minutes
	
	@Bean
    public HttpClientConfig httpClientConfig() {
    	HttpClientConfig cfg = new HttpClientConfig();
    	cfg.setMaxConnection(maxConnection);
    	cfg.setMaxConnectionPerRoute(maxConnectionPerRoute);
    	cfg.setMaxIdleTime(maxIdleTime);
    	cfg.setTimeToLive(timeToLive);
    	return cfg;
    }
	
	@Bean
	public HttpClientGenerator httpClientGenerator(HttpClientConfig httpClientConfig) {
		return new HttpClientGenerator(httpClientConfig);
	}
	
    @Bean
    public ImprovedDownloader downloaderManager(HttpClientGenerator httpClientGenerator) {
        return new DownloaderManager(httpClientGenerator);
    }
    
    @Bean
    public ProxyPoolManager proxyPoolManager(ProxyStoreLoader proxyStoreLoader) {
        ProxyPoolManager httpProxy = new ProxyPoolManager();
        httpProxy.setProxyPool(new FastProxyPool());
        httpProxy.setHttpProxyStoreLoader(proxyStoreLoader);
        
        return httpProxy;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        return restTemplate;
    }
    
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        final int timeout = (int)TimeUnit.SECONDS.toMillis(10);
        RequestConfig config = RequestConfig
                .custom()
                // ???????????????????????????????????????????????????????????????????????????????????? ?????????
                //   org.apache.http.conn.ConnectionPoolTimeoutException:
                //     Timeout waiting for connection from pool
                .setConnectionRequestTimeout(timeout / 2)
                // ??????????????????(????????????)?????????????????????????????????connect timeout
                .setConnectTimeout(timeout / 2)
                // ?????????????????????(response)?????????????????????????????????read timeout
                .setSocketTimeout(timeout)
                .build();

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(true)
                .setTcpNoDelay(false)
                .setSoReuseAddress(true)
                .setSoTimeout(timeout)
                .setSndBufSize(32768)  // 32K
                .build();
        
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(100);
        cm.setDefaultSocketConfig(socketConfig);
        cm.closeExpiredConnections();
        cm.closeIdleConnections(500, TimeUnit.MILLISECONDS);
        
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(config)
                .setDefaultSocketConfig(socketConfig)
                .setConnectionManager(cm)
                .setDefaultHeaders(ImmutableList.of(new BasicHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE)))
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }
}
