package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@ResponseBody
public class KeepalivedController {
	@RequestMapping("/keepalived")
	public String keepalived() {
		return "OK";
	}

	@Value("${onespider.store.application.name}")
	private String storeApplicationName;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/test")
	public String testServer() {
		String url = "http://"+storeApplicationName+"/superAddress";
		System.out.println(url);
		String callServiceResult = restTemplate.getForObject( url, String.class);
		return callServiceResult;
	}

}