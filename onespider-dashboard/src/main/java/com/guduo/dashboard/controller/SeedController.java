package com.guduo.dashboard.controller;

import java.io.IOException;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.common.util.EnumUtils;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.data.table.Seed;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年3月16日 下午6:15:58
 *
 */
@Controller
@RequestMapping("/seed")
@CommonsLog
public class SeedController {


    @Value("${onespider.store.host}")
    private String storePath;
    
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("")
    public String toSeedPage(Model model){
        Paginator<Seed> paginator = new Paginator<Seed>();
        model.addAttribute("paginator", paginator);
        model.addAttribute("platform", EnumUtils.covertPlatformToMap());
        return "seed";
    }

    @GetMapping("/list")
    public String getSeed(
            @RequestParam(value="code", required = false) String code,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size,
            Model model) throws IOException {
        String apiUrl = storePath + "/seed/getlist";
        String uri = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("code", code)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .encode()
                .toString();

        Paginator<Seed> paginator = restTemplate.getForObject(uri, Paginator.class);

        model.addAttribute("paginator", paginator);
        model.addAttribute("platform", EnumUtils.covertPlatformToMap());

        return "seed";
    }
    
    @GetMapping("/modify")
    @ResponseBody
    public Object modifySeed(
            @RequestParam("id") Integer id,
            @RequestParam(name = "frequency", required = false) Integer frequency,
            @RequestParam(name = "status", required = false) Integer status) throws IOException{
        String apiUrl = storePath + "/seed/modify";
        String uri = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("id", id)
                .queryParam("frequency", frequency)
                .queryParam("status", status)
                .build()
                .encode()
                .toString();
        
        return Request.Get(uri).execute().returnContent().asString();
    }
}
