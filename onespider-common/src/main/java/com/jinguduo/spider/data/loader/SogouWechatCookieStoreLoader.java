package com.jinguduo.spider.data.loader;

import com.jinguduo.spider.data.table.WechatSogouCookie;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lc on 2019/4/30
 */
@CommonsLog
public class SogouWechatCookieStoreLoader implements StoreLoader<Integer, List<WechatSogouCookie>>, InitializingBean {

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Value("${onespider.store.sogou_wechat.cookie.url}")
    private String url;


    public SogouWechatCookieStoreLoader() {
    }

    public SogouWechatCookieStoreLoader(RestTemplate restTemplate, String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }


    @Override
    public List<WechatSogouCookie> load(Integer size) {
        List<WechatSogouCookie> devices = null;
        try {
            if (url == null) {
                log.warn("The url maybe null.");
                return devices;
            }
            String query = UriComponentsBuilder.fromHttpUrl(url)
                    .build()
                    .toUriString();
            WechatSogouCookie[] resp = restTemplate.getForObject(query, WechatSogouCookie[].class);
            devices = Arrays.asList(resp);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
        }

        return devices;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (restTemplate == null) {
            log.warn("The restTemplate maybe null.");
            restTemplate = new RestTemplate();
        }
    }
}
