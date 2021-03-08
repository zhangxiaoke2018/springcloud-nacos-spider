package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RefreshScope
public class KeepalivedController {

	@Value("${onespider.supervisor.path}")
	public String superVisorAddress;

	@RequestMapping("/keepalived")
	public String keepalived() {
		return "OK";
	}

	@RequestMapping("/superAddress")
	public String getSuperVisorAddress() {
		return superVisorAddress;
	}
}
