package com.jinguduo.spider.access;

import com.jinguduo.spider.data.table.StockCompany;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.data.table.Pulse;
import com.jinguduo.spider.data.table.Seed;

import java.util.List;

@CommonsLog
@Component
public class OneAccessor {

    @Value("${onespider.store.seed_all_time.url}")
    private String getSeedAllTimeStoreUrl;

    @Value("${onespider.store.pulse.all.url}")
    private String pulseUrl;

    @Value("${onespider.store.host.job_stock_company}")
    private String stockUrl;

    @Value("${onespider.store.application.name}")
    private String storeApplicationName;

    @Autowired
    private RestTemplate restTemplate;

    private String httpType="http://";


    public List<Job> fetchJobs(String path) {
        ParameterizedTypeReference<List<Job>> typeRef = new ParameterizedTypeReference<List<Job>>() {};
        ResponseEntity<List<Job>> resp = restTemplate.exchange(path, HttpMethod.GET, null, typeRef);
        return resp.getBody();
    }


    public List<Seed> fetchSeeds(Long time){
        ParameterizedTypeReference<List<Seed>> typeRef = new ParameterizedTypeReference<List<Seed>>() {};
        ResponseEntity<List<Seed>> resp = restTemplate.exchange(httpType+storeApplicationName+getSeedAllTimeStoreUrl+"?loadtime=" + String.valueOf(time), HttpMethod.GET, null, typeRef);
        return resp.getBody();
    }

    public List<Pulse> fetchPulse(){
        ParameterizedTypeReference<List<Pulse>> typeRef = new ParameterizedTypeReference<List<Pulse>>() {};
        ResponseEntity<List<Pulse>> resp = restTemplate.exchange(httpType+storeApplicationName+pulseUrl, HttpMethod.GET, null, typeRef);
        return resp.getBody();
    }

    public List<StockCompany> fetchStock(){
        ParameterizedTypeReference<List<StockCompany>> typeRef = new ParameterizedTypeReference<List<StockCompany>>() {};
        ResponseEntity<List<StockCompany>> resp = restTemplate.exchange(httpType+storeApplicationName+stockUrl, HttpMethod.GET, null, typeRef);
        return resp.getBody();
    }

}
