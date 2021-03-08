package com.jinguduo.spider.data.loader;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.common.constant.UserAgentKind;
import com.jinguduo.spider.data.table.UserAgent;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class UserAgentStoreLoader implements StoreLoader<UserAgentKind, Collection<UserAgent>> {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Value("${onespider.store.user_agent.url}")
    private String url = null;
    
    public UserAgentStoreLoader() {
    }

    public UserAgentStoreLoader(RestTemplate restTemplate, String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @Override
    public Collection<UserAgent> load(UserAgentKind kind) {
        Collection<UserAgent> userAgents = null;
        try {
            if (restTemplate == null) {
                restTemplate = new RestTemplate();
            }
            String query = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("kind", kind)
                    .build()
                    .toUriString();
            UserAgent[] resp = restTemplate.getForObject(query, UserAgent[].class);
            userAgents = Arrays.asList(resp);
            
        } catch (Exception e) {
            log.error(kind, e);
        }
        return userAgents;
    }
}
