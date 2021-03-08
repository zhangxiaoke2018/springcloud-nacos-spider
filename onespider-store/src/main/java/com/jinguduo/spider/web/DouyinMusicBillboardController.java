package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinMusicBillboard;
import com.jinguduo.spider.service.DouyinMusicBillboardService;

@Controller
@ResponseBody
public class DouyinMusicBillboardController {

	@Autowired
	private DouyinMusicBillboardService douyinMusicBillboardService;
	
	@PostMapping("/douyin/music_billboards")
	public String saveList(@RequestBody List<DouyinMusicBillboard> billboard) {
		douyinMusicBillboardService.save(billboard);
		return "OK";
	}
}
