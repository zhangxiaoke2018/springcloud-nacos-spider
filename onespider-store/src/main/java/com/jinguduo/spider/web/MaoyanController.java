package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.MaoyanActor;
import com.jinguduo.spider.service.MaoyanService;

@RestController
@RequestMapping("/maoyan")
public class MaoyanController {

	@Autowired
	private MaoyanService maoyanService;

	@RequestMapping(value = "/actor", method = RequestMethod.POST)
	public String insert(@RequestBody MaoyanActor actor) {
		maoyanService.insertOrUpdate(actor);
		return "ok";
	}

}
