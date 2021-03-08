package com.jinguduo.spider.data.loader;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.data.table.CookieString;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class CookieStringStoreLoader implements StoreLoader<String, List<CookieString>>, InitializingBean {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Value("${onespider.store.cookie_string.url}")
    private String url;
    
    private int pageSize = 500;
    
    public CookieStringStoreLoader() {
    }

    public CookieStringStoreLoader(RestTemplate restTemplate, String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @Override
    public List<CookieString> load(String domain) {
        List<CookieString> cookieStrings = null;
        try {
            if (url == null) {
                log.warn("The url maybe null.");
                return cookieStrings;
            }
            String query = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("domain", domain)
                    .queryParam("size", pageSize)
                    .build()
                    .toUriString();
            CookieString[] resp = restTemplate.getForObject(query, CookieString[].class);
            cookieStrings = Arrays.asList(resp);
        } catch (Exception e) {
            log.error(domain, e);
        }
        return cookieStrings;
    }

    public void save(CookieString cookieString) {
        try {
            if (url == null) {
                log.warn("The url maybe null.");
                return;
            }
            restTemplate.postForObject(url, cookieString, String.class);
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
