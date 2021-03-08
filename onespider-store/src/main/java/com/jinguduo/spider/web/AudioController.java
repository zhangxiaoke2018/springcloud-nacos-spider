package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.service.AudioService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audio")
@Slf4j
public class AudioController {

    @Autowired
    private AudioService audioService;

    @RequestMapping(value="/meta",method = RequestMethod.POST)
	public String saveAudio(@RequestBody Audio audio) {
    	
    	try {
    		return audioService.saveAudio(audio);
    	}catch(Exception e) {
    		log.info("insert audio error:{} ,exception: {}",audio,e.getClass().getName());
    		return "Save Failed:"+audio.toString();
    	}
	}
    
    @RequestMapping(value="/volume",method = RequestMethod.POST)
	public String saveVolume(@RequestBody AudioVolumeLog volume) {
    	
    	try {
    		return audioService.saveAudioVolumeLog(volume);
    	}catch(Exception e) {
    		log.info("insert audio error:{} ,exception: {}",volume,e.getClass().getName());
    		return "Save Failed:"+volume.toString();
    	}
	}
    
    @RequestMapping(value="/playcount",method = RequestMethod.POST)
	public String savePlaycount(@RequestBody AudioPlayCountLog playcount) {
    	
    	try {
    		return audioService.insertAudioPlayCount(playcount);
    	}catch(Exception e) {
    		log.info("insert audio error:{} ,exception: {}",playcount,e.getClass().getName());
    		return "Save Failed:"+playcount.toString();
    	}
	}
    
}
