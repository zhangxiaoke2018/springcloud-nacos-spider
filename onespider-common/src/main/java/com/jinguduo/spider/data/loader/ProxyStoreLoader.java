package com.jinguduo.spider.data.loader;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.data.table.Proxy;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class ProxyStoreLoader implements StoreLoader<ProxyState, Collection<Proxy>>, InitializingBean {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Value("${onespider.store.proxy.url}")
    private String url = null;
    
    public ProxyStoreLoader() {
    }

    public ProxyStoreLoader(RestTemplate restTemplate, String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @Override
    public List<Proxy> load(ProxyState state) {
        List<Proxy> proxies = null;
        try {
            if (url == null || "/proxies".equals(url)) {
                log.warn("The url maybe null.");
                return proxies;
            }
            String query = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("state", state)
                    .queryParam("size", 2000)
                    .build()
                    .toUriString();
            
            ParameterizedTypeReference<List<Proxy>> typeRef = new ParameterizedTypeReference<List<Proxy>>() {};
            ResponseEntity<List<Proxy>> resp = restTemplate.exchange(query, HttpMethod.GET, null, typeRef);
            proxies = resp.getBody();
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return proxies;
    }
    
    public void save(Proxy httpProxy) {
        List<Proxy> req = Arrays.asList(httpProxy);
        save(req);
    }
    
    public void save(List<Proxy> httpProxies) {
        try {
            if (url == null) {
                log.warn("The url maybe null.");
                return;
            }
            restTemplate.postForObject(url, httpProxies, String.class);
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (restTemplate == null) {
            log.warn("The restTemplate maybe null.");
            restTemplate = new RestTemplate();
        }
    }
}
