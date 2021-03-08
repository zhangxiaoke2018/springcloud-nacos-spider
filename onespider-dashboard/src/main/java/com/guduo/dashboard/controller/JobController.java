package com.guduo.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.cluster.model.JobWrapper;
import com.jinguduo.spider.common.util.Paginator;

/**
 * 爬虫在运行任务及状态
 * 
 *
 */
@Controller
public class JobController {

    @Value("${onespider.master.job.restful.url}")
    private String jobRestfulUrl;

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    public String doList(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size,
            Model model) {

        String uri = UriComponentsBuilder
            .fromHttpUrl(jobRestfulUrl)
            .queryParam("code", code)
            .queryParam("page", page)
            .queryParam("size", size)
            .build()
            .encode()
            .toString();

        Paginator<JobWrapper> paginator = restTemplate.getForObject(uri, Paginator.class);

        model.addAttribute("paginator", paginator);

        return "jobs";
    }
}
