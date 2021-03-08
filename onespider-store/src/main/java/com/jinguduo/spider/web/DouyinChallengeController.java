package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinChallenge;
import com.jinguduo.spider.service.DouyinChallengeService;

@Controller
@ResponseBody
public class DouyinChallengeController {

	@Autowired
	private DouyinChallengeService douyinChallengeService;
	
	@PostMapping("/douyin/challenges")
	public String saveList(@RequestBody List<DouyinChallenge> challenges) {
		douyinChallengeService.save(challenges);
		return "OK";
	}
}
