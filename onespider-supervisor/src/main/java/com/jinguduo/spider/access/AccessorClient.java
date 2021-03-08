package com.jinguduo.spider.access;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("spider-store")
public interface AccessorClient {

    @GetMapping("/seed/allbytime")
    public List fetchSeeds(@RequestParam("loadtime")Long time);

    @GetMapping("/pulse/all")
    public List fetchPulse();


}
