package com.guduo.dashboard.controller;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
public class KeepalivedController {


	@RequestMapping("/keepalived")
	public String keepalived() {
		return "OK";
	}

	@NacosInjected
	private NamingService namingService;


	@NacosValue(value = "${useLocalCache:0}", autoRefreshed = true)
	private String useLocalCache;


	@RequestMapping(value = "/get")
	@ResponseBody
	public String get() {
		return useLocalCache;
	}

	@RequestMapping(value = "/getInstances")
	@ResponseBody
	public List<Instance> get(@RequestParam String serviceName) throws NacosException {
		return namingService.getAllInstances(serviceName);
	}

	@RequestMapping(path = "/hello")
	public String hello(@RequestParam(name = "name") String name) {
		return String.format("%s say hello!", name);
	}




}
