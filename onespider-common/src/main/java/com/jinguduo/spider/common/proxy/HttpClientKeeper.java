package com.jinguduo.spider.common.proxy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.jinguduo.spider.common.keeper.ResourceKeeper;
import com.jinguduo.spider.common.net.FakedHttpsConnectionSocketFactory;
import com.jinguduo.spider.common.net.FractionDnsResolver;
import com.jinguduo.spider.common.net.HttpConnectionSocketFactory;
import com.jinguduo.spider.common.thread.TimeDelay;
import com.jinguduo.spider.common.type.Quota;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpClientKeeper implements ResourceKeeper<CloseableHttpClient> {
	
	private final ReentrantLock lock = new ReentrantLock();
	
    private final static int SOCKET_TIMEOUT = 12000;
    
    private static SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(SOCKET_TIMEOUT)
            .setSoKeepAlive(false)
            .setSoReuseAddress(false)
            .setTcpNoDelay(true)
            .build();
    
    private Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", HttpConnectionSocketFactory.INSTANCE)
            .register("https", FakedHttpsConnectionSocketFactory.INSTANCE)
            .build();
    
    private volatile CloseableHttpClient httpClient = null;
	
	private Quota quota;
	
	@Override
	public CloseableHttpClient create() {
		return HttpClients.custom()
	            .setConnectionManager(new PoolingHttpClientConnectionManager(registry,
	            		FractionDnsResolver.INSTANCE))
	            .setDefaultSocketConfig(socketConfig)
	            .setConnectionTimeToLive(1, TimeUnit.MINUTES)
	            .evictExpiredConnections()
	            .evictIdleConnections(10, TimeUnit.SECONDS)
	            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1 Safari/605.1.15")
	            .setMaxConnPerRoute(40)
	            .setMaxConnTotal(80)
	            .build();
	}

	@Override
	public void close(CloseableHttpClient httpClient) {
		TimeDelay.execute(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Goto HttpClient Close");
					httpClient.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}, 1, TimeUnit.MINUTES);
	}

	@Override
	public CloseableHttpClient get() {
		if (httpClient == null || (quota != null && quota.isAboved(null))) {
			lock.lock();
			try {
				// double check
				if (httpClient == null || (quota != null && quota.isAboved(null))) {
					CloseableHttpClient oldHttpClient = httpClient;
					this.httpClient = create();
					this.quota.reset(null);
					if (oldHttpClient != null) {
						this.close(oldHttpClient);
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return this.httpClient;
	}

	@Override
	public ResourceKeeper<CloseableHttpClient> setQuota(Quota quota) {
		this.quota = quota;
		return this;
	}

}
