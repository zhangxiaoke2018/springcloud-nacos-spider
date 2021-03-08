package com.jinguduo.spider.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.db.repo.AudioPlayCountRepo;
import com.jinguduo.spider.db.repo.AudioRepo;
import com.jinguduo.spider.db.repo.AudioVolumeRepo;

@Service
public class AudioService {
	@Autowired
	private AudioRepo audioRepo;

	@Autowired
	private AudioVolumeRepo audioVolumeRepo;

	@Autowired
	private AudioPlayCountRepo audioPlayCountRepo;

	public String saveAudio(Audio nw) {
		if (!checkObject(nw))
			return "INVALID ITEM";

		Audio old = audioRepo.findOneByCodeAndPlatformId(nw.getCode(), nw.getPlatformId());

		if (old != null)
			nw = updateAudio(nw, old);

		nw = audioRepo.save(nw);
		
		return nw.toString();
	}
	
	public String saveAudioVolumeLog(AudioVolumeLog nw) {
		if (!checkObject(nw))
			return "INVALID ITEM";
		
		AudioVolumeLog old = audioVolumeRepo.findOneByCodeAndPlatformIdAndDay(nw.getCode(), nw.getPlatformId(),nw.getDay());
		if(old!=null) {
			nw.setId(old.getId());
		}
		
		nw = audioVolumeRepo.save(nw);
		
		return nw.toString();
	}
	
	public String insertAudioPlayCount(AudioPlayCountLog nw) {
		if (!checkObject(nw))
			return "INVALID ITEM";
		
		nw = audioPlayCountRepo.save(nw);
		
		return nw.toString();
	}

	private Audio updateAudio(Audio nw, Audio old) {
		nw.setId(old.getId());
		if (!old.getTags().equals(nw.getTags())) {
			String tag = old.getTags() + "," + nw.getTags();
			String[] ts = Arrays.stream(tag.split(",")).filter(s -> StringUtils.hasText(s)).distinct()
					.toArray(String[]::new);
			nw.setTags(String.join(",", ts));
		}
		
		if (!StringUtils.hasText(nw.getCover()) && StringUtils.hasText(old.getCover())) {
			nw.setCover(old.getCover());
		}

		if (!StringUtils.hasText(nw.getIntroduction()) && StringUtils.hasText(old.getIntroduction())) {
			nw.setIntroduction(old.getIntroduction());
		}

		return nw;
	}

	private boolean checkObject(Object item) {
		if (item instanceof Audio) {
			Audio audio = (Audio) item;
			return StringUtils.hasText(audio.getName()) && StringUtils.hasText(audio.getPublisher())
					&& StringUtils.hasText(audio.getCode()) && audio.getPlatformId() != null
					&& audio.getPlatformId() > 0;
		}else if (item instanceof AudioVolumeLog) {
			AudioVolumeLog vlog = (AudioVolumeLog) item;
			return StringUtils.hasText(vlog.getCode()) && vlog.getPlatformId() != null
					&& vlog.getPlatformId() > 0
					&& vlog.getVolumes()!=null
					&& vlog.getVolumes() >0
					&& vlog.getDay() != null;
		}else if (item instanceof AudioPlayCountLog) {
			AudioPlayCountLog pv = (AudioPlayCountLog) item;
			return StringUtils.hasText(pv.getCode()) && pv.getPlatformId() != null
					&& pv.getPlatformId() > 0
					&& pv.getPlayCount()!=null
					&& pv.getPlayCount() >0;
		}
		return false;
	}
}
