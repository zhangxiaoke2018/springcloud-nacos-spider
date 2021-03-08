package com.guduo.dashboard.controller;

import java.io.IOException;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.data.table.SpiderSetting;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年3月17日 上午11:25:03
 *
 */
@Controller
@RequestMapping("/setting")
@CommonsLog
public class SpiderSettingController {


    @Value("${onespider.store.host}")
    private String storePath;
    
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("")
    public String toSettingPage(Model model){
        Paginator<SpiderSetting> paginator = new Paginator<SpiderSetting>();
        model.addAttribute("paginator", paginator);
        return "setting";
    }

    @GetMapping("/list")
    public String getSetting(
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size,
            Model model) throws IOException {
        String apiUrl = storePath + "/spider_setting/getlist";
        String uri = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("domain", domain)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .encode()
                .toString();

        Paginator<SpiderSetting> paginator = restTemplate.getForObject(uri, Paginator.class);

        model.addAttribute("paginator", paginator);

        return "setting";
    }
    
    @GetMapping("/get")
    public String toModifyPage(
            @RequestParam("id") String id,
            @RequestParam(name="errMsg",required = false) String errMsg,
            Model model){
        //errMsg修改失败后重定向携带的错误提示
        String apiUrl = storePath + "/spider_setting/getOne";
        String uri = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("id", id)
                .build()
                .encode()
                .toString();

        SpiderSetting s = restTemplate.getForObject(uri, SpiderSetting.class);
        if(StringUtils.isNotBlank(errMsg)){
            model.addAttribute("errMsg", errMsg);
        }
        model.addAttribute("res", s);

        return "modify_setting";
    }
    
    @PostMapping("/modify")
    public String modifySeed(@ModelAttribute SpiderSetting ss, Model model) throws IOException{
        String apiUrl = storePath + "/spider_setting/modify";
        String uri = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("id", ss.getId())
                .queryParam("threadNum", ss.getThreadNum())
                .queryParam("sleepTime", ss.getSleepTime())
                .queryParam("retryTimes", ss.getRetryTimes())
                .queryParam("timeOut", ss.getTimeOut())
                .queryParam("emptySleepTime", ss.getEmptySleepTime())
                .queryParam("frequency", ss.getFrequency())
                .queryParam("domain", ss.getDomain())
                .queryParam("httpProxyEnabled", ss.getHttpProxyEnabled())
                .build()
                .encode()
                .toString();
        
        String res = Request.Get(uri).execute().returnContent().asString();
        if(StringUtils.equals(res, "SUCCESS")){
            return "redirect:/setting/list";
        }else{
            return "redirect:/setting/get?id="+ss.getId()+"&errMsg="+res;
        }
    }
}
