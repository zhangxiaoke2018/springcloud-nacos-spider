package com.jinguduo.spider.data.loader;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.data.table.DouyinDevice;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class DouyinDeviceStoreLoader implements StoreLoader<Integer, List<DouyinDevice>>, InitializingBean {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Value("${onespider.store.douyin.device.url}")
    private String url;
    
    public DouyinDeviceStoreLoader() {
    }

    public DouyinDeviceStoreLoader(RestTemplate restTemplate, String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @Override
    public List<DouyinDevice> load(Integer size) {
        List<DouyinDevice> devices = null;
        try {
            if (url == null) {
                log.warn("The url maybe null.");
                return devices;
            }
            String query = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("size", size)
                    .build()
                    .toUriString();
            DouyinDevice[] resp = restTemplate.getForObject(query, DouyinDevice[].class);
            devices = Arrays.asList(resp);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return devices;
    }

    public void save(List<DouyinDevice> douyinDevices) {
        try {
            if (url == null) {
                log.warn("The url maybe null.");
                return;
            }
            restTemplate.postForObject(url, douyinDevices, String.class);
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
