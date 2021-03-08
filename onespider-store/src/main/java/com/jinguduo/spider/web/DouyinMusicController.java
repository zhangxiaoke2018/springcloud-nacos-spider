package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinMusic;
import com.jinguduo.spider.service.DouyinMusicService;

@Controller
@ResponseBody
public class DouyinMusicController {

	@Autowired
	private DouyinMusicService douyinMusicService;
	
	@PostMapping("/douyin/musics")
	public String saveList(@RequestBody List<DouyinMusic> musics) {
		douyinMusicService.save(musics);
		return "OK";
	}
}
