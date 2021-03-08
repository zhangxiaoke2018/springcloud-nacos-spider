package com.guduo.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.guduo.dashboard.vo.Worker;
import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.common.util.Paginator;

@Controller
public class WorkerController {
    
    @Value("${onespider.master.worker.restful.url}")
    private String workerRestfulUrl;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/workers")
    public String doList(
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size,
            Model model) {

        String uri = UriComponentsBuilder
            .fromHttpUrl(workerRestfulUrl)
            .queryParam("domain", domain)
            .queryParam("page", page)
            .queryParam("size", size)
            .build()
            .encode()
            .toString();

        ParameterizedTypeReference<Paginator<Worker>> typeRef = new ParameterizedTypeReference<Paginator<Worker>>() {};
        ResponseEntity<Paginator<Worker>> resp = restTemplate.exchange(uri, HttpMethod.GET, null, typeRef);

        model.addAttribute("paginator", resp.getBody());

        return "workers";
    }
    
    @PostMapping(value = "/workers")
    @ResponseBody
    public Worker doPost(
            @RequestParam(value = "uuid") String uuid,
            @RequestParam(value = "command") WorkerCommand command) {
        
        String url = UriComponentsBuilder
                .fromHttpUrl(workerRestfulUrl)
                .queryParam("uuid", uuid)
                .queryParam("command", command)
                .build()
                .encode()
                .toString();
        
        ParameterizedTypeReference<Worker> typeRef = new ParameterizedTypeReference<Worker>() {};
        ResponseEntity<Worker> resp = restTemplate.exchange(url, HttpMethod.POST, null, typeRef);
        
        return resp.getBody();
    }
}
