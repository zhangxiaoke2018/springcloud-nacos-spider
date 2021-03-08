package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.DouyinDevice;
import com.jinguduo.spider.service.DouyinDeviceService;

@Controller
@ResponseBody
public class DouyinDeviceController {
	
	@Autowired
	private DouyinDeviceService douyinDeviceService;

	@PostMapping("/douyin/devices")
	public String saveList(@RequestBody List<DouyinDevice> devices) {
		douyinDeviceService.save(devices);
		return "OK";
	}
	
	@GetMapping("/douyin/devices")
	public Iterable<DouyinDevice> getList(
            @RequestParam(name = "size", defaultValue = "1000", required = false) Integer size) {
		return douyinDeviceService.findByRandomSorted(size);
	}
}
