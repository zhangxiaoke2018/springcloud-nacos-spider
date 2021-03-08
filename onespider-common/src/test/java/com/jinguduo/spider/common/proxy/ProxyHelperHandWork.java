package com.jinguduo.spider.common.proxy;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.data.table.Proxy;

@ActiveProfiles("test")
@Ignore("Handwork")
public class ProxyHelperHandWork {
    
    @Test
    public void testValidateUnknownProxy1() {
        
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.unknown);
        proxy.setHost("127.0.0.1:1086");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        
        Assert.isTrue(r, "bad");
    }
    
    @Test
    public void testValidateUnknownProxy2() {
        
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.unknown);
        proxy.setHost("127.0.0.1:1087");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        
        Assert.isTrue(r, "bad");
    }
    
    @Test
    public void testValidateUnknownProxy3() {
        
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.unknown);
        proxy.setHost("116.62.189.152:1080");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        Assert.isTrue(r, "bad");
    }

    @Test
    public void testValidateSocksProxy() {
        
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.socks);
        proxy.setHost("127.0.0.1:1086");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        Assert.isTrue(r, "bad");
        
        Proxy proxy2 = new Proxy();
        proxy2.setPtype(ProxyType.socks);
        proxy2.setHost("127.0.0.1:1086");
        
        boolean r2 = ProxyHelper.validateProxy(proxy2);
        Assert.isTrue(r2, "bad");
    }
    
    @Test
    public void testValidateSocks5Proxy() {
        
        Proxy proxy = Proxy.newSocks5Proxy();
        proxy.setHost("127.0.0.1:1086");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        Assert.isTrue(r, "bad");
        
        Proxy proxy2 = Proxy.newSocks5Proxy();
        proxy2.setHost("127.0.0.1:1086");
        
        boolean r2 = ProxyHelper.validateProxy(proxy2);
        Assert.isTrue(r2, "bad");
    }
    
    @Test
    public void testValidateSocks4Proxy() {
        
        Proxy proxy = Proxy.newSocks4Proxy();
        proxy.setHost("116.62.189.152:1080");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        Assert.isTrue(r, "bad");
        
        // bad case
        Proxy proxy2 = Proxy.newSocks5Proxy();
        proxy2.setHost("221.203.169.50:1080");
        
        boolean r2 = ProxyHelper.validateProxy(proxy2);
        Assert.isTrue(!r2, "bad");
    }
    
    @Test
    public void testValidateHttpProxy() {
        
        Proxy proxy = Proxy.newHttpProxy();
        proxy.setHost("127.0.0.1:1087");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        
        Assert.isTrue(r, "bad");
    }
    
    @Test
    public void testValidateHttpsProxy() {
        
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.https);
        proxy.setHost("127.0.0.1:1087");
        
        boolean r = ProxyHelper.validateProxy(proxy);
        
        Assert.isTrue(r, "bad");
    }
    
    @Test
    public void testValidateProxyVsValidateSocketConnection() {
    	RestTemplate restTemplate = new RestTemplate();
    	ParameterizedTypeReference<List<Proxy>> typeRef = new ParameterizedTypeReference<List<Proxy>>() {};
    	
    	String query = UriComponentsBuilder.fromHttpUrl("http://120.27.83.6:9800/proxies")
                .queryParam("state", ProxyState.Vps)
                .queryParam("size", 20)
                .build()
                .toUriString();
    	
        ResponseEntity<List<Proxy>> resp = restTemplate.exchange(query, HttpMethod.GET, null, typeRef);
        List<Proxy> proxies = resp.getBody();
        
        proxies.sort(Comparator.comparing(Proxy::getServerName));
        final long ALIVED_TIME_MILLIS = TimeUnit.SECONDS.toMillis(40);
        
        StringBuilder sb = new StringBuilder();
        for (Proxy proxy : proxies) {
        	sb.append("ServerName:").append(proxy.getServerName());
        	final long t0 = System.currentTimeMillis();
        	if ((t0 - proxy.getUpdatedAt().getTime()) >= ALIVED_TIME_MILLIS) {
        		// 跳过
				sb.append("\t Result:").append("-")
					.append("\r\n");
        	} else {
        		boolean right = ProxyHelper.validateProxy(proxy);
        		final long t1 = System.currentTimeMillis();
        		boolean left = ProxyHelper.validateSocketConnection(proxy);
        		final long t2 = System.currentTimeMillis();
        		
        		Assert.isTrue(right == left, proxy.toString());
        		
        		sb.append("\t Result:").append(right)
        			.append("\t Socket:").append(t2 - t1)
        			.append("\t HTTPs:").append(t1 - t0)
        			.append("\t IP:").append(proxy.getHost())
	        		.append("\r\n");
        	}
		}
        System.out.println(sb.toString());
    }
    
    @Test
    public void testValidateSocketConnection() {
    	RestTemplate restTemplate = new RestTemplate();
    	ParameterizedTypeReference<List<Proxy>> typeRef = new ParameterizedTypeReference<List<Proxy>>() {};
    	
    	String query = UriComponentsBuilder.fromHttpUrl("http://120.27.83.6:9800/proxies")
                .queryParam("state", ProxyState.Vps)
                .queryParam("size", 20)
                .build()
                .toUriString();
    	
        ResponseEntity<List<Proxy>> resp = restTemplate.exchange(query, HttpMethod.GET, null, typeRef);
        List<Proxy> proxies = resp.getBody();
        // 每次固定顺序
        proxies.sort(Comparator.comparing(Proxy::getServerName));
        
        final long ALIVED_TIME_MILLIS = TimeUnit.SECONDS.toMillis(50);
        StringBuilder sb = new StringBuilder();
        for (Proxy proxy : proxies) {
        	sb.append("ServerName:").append(proxy.getServerName());
        	final long t0 = System.currentTimeMillis();
        	if ((t0 - proxy.getUpdatedAt().getTime()) >= ALIVED_TIME_MILLIS) {
        		// 跳过
				sb.append("\t Result:").append("-")
					.append("\r\n");
			} else {
				boolean right = ProxyHelper.validateSocketConnection(proxy);
				final long t1 = System.currentTimeMillis();
				
				sb.append("\t Result:").append(right)
					.append("\t Socket:").append(t1 - t0)
					.append("\t IP:").append(proxy.getHost())
					.append("\r\n");
			}
		}
        System.out.println(sb.toString());
    }
}
