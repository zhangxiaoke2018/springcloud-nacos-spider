package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class EnvironmentController {
	
	@Autowired
	private Environment env;

	@GetMapping({"/env", "/env/{key:.+}"})
	public String get(@PathVariable(value = "key", required = false) String key) {
		String v = null;
		if (StringUtils.isEmpty(key)) {
			v = "ActiveProfiles=" + String.join(", ", env.getActiveProfiles());
		} else {
			v = env.getProperty(key, "None");
		}
		return v;
	}
}
