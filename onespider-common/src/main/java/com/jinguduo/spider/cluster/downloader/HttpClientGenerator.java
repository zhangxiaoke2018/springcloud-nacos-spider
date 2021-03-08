package com.jinguduo.spider.cluster.downloader;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.common.net.FakedHttpsConnectionSocketFactory;
import com.jinguduo.spider.common.net.FractionDnsResolver;
import com.jinguduo.spider.common.net.HttpConnectionSocketFactory;
import com.jinguduo.spider.webmagic.downloader.CustomRedirectStrategy;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class HttpClientGenerator {

	private PoolingHttpClientConnectionManager connectionManager;
	
	public HttpClientGenerator() {
		this(new HttpClientConfig());
	}
	
    public HttpClientGenerator(HttpClientConfig config) {
    	Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
    			.register("http", HttpConnectionSocketFactory.INSTANCE)
    			.register("https", FakedHttpsConnectionSocketFactory.INSTANCE)
    			.build();
    	
    	/* See {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager}
    	 *   maintains a maximum limit of connection on a per route basis and in total.
    	 */
    	connectionManager = new PoolingHttpClientConnectionManager(reg,
    			ManagedHttpClientConnectionFactory.INSTANCE,
    			null,
    			FractionDnsResolver.INSTANCE,
    			config.getTimeToLive(), TimeUnit.MINUTES);
    	
    	connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionPerRoute());
    	connectionManager.setMaxTotal(config.getMaxConnection());
    	connectionManager.closeIdleConnections(config.getMaxIdleTime(), TimeUnit.MILLISECONDS);
    	connectionManager.closeExpiredConnections();
    	
    	if (log.isDebugEnabled()) {
    		log.debug(config.toString());
    	}
    }

    /**
     * 
     * 
     * @param poolSize
     * @return
     */
    public HttpClientGenerator setPoolSize(int poolSize) {
    	// no-op
        return this;
    }

    public CloseableHttpClient getClient(Site site) {
        return generateClient(site);
    }

    private CloseableHttpClient generateClient(Site site) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(true);
        if (site.getUserAgent() != null) {
            httpClientBuilder.setUserAgent(site.getUserAgent());
        } else {
            httpClientBuilder.setUserAgent("-");
        }
        if (site.isUseGzip()) {
            httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {

                public void process(
                        final HttpRequest request,
                        final HttpContext context) throws HttpException, IOException {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }

                }
            });
        }
        //解决post/redirect/post 302跳转问题
        httpClientBuilder.setRedirectStrategy(new CustomRedirectStrategy());
        
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(false)
                .setTcpNoDelay(true)
                .setSoTimeout(site.getTimeOut())
                .setSoReuseAddress(false)
                .build();
        connectionManager.setDefaultSocketConfig(socketConfig);
        httpClientBuilder.setDefaultSocketConfig(socketConfig);
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(site.getRetryTimes(), true));
        generateCookie(httpClientBuilder, site);
        return httpClientBuilder.build();
    }

    private final static long ONE_YEAR = TimeUnit.DAYS.toMillis(365);
    private void generateCookie(HttpClientBuilder httpClientBuilder, Site site) {
        CookieStore cookieStore = new BasicCookieStore();
        for (Map.Entry<String, String> cookieEntry : site.getCookies().entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(site.getDomain());
            cookie.setAttribute(ClientCookie.DOMAIN_ATTR, site.getDomain());
            cookie.setExpiryDate(new Date(System.currentTimeMillis() + ONE_YEAR));
            cookieStore.addCookie(cookie);
        }
        for (Map.Entry<String, Map<String, String>> domainEntry : site.getAllCookies().entrySet()) {
            for (Map.Entry<String, String> cookieEntry : domainEntry.getValue().entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
                cookie.setDomain(domainEntry.getKey());
                cookie.setAttribute(ClientCookie.DOMAIN_ATTR, domainEntry.getKey());
                cookie.setExpiryDate(new Date(System.currentTimeMillis() + ONE_YEAR));
                cookieStore.addCookie(cookie);
            }
        }
        httpClientBuilder.setDefaultCookieStore(cookieStore);
    }

}

