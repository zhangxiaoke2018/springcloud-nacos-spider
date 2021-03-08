package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.AdLogs;
import com.jinguduo.spider.data.table.Ads;
import com.jinguduo.spider.db.repo.AdLogsRepo;
import com.jinguduo.spider.db.repo.AdsRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public class AdLogService {

	@Autowired
	private AdLogsRepo adLogsRepo;

	@Autowired
	private AdsRepo adsRepo;


	public AdLogs insert(AdLogs adLogs) {
		assert adLogs != null;

		if (StringUtils.isBlank(adLogs.getFileName())){
			return null;
		}

		Ads ads = adsRepo.findByFileName(adLogs.getFileName());
		if (ads == null) {
			Ads a = new Ads();
			a.setFileName(adLogs.getFileName());
			a.setPlatformId(adLogs.getPlatformId());
			a.setCategory(adLogs.getCategory());
			ads = adsRepo.save(a);
		} else {
			ads.setCategory(adLogs.getCategory());
			ads.setPlatformId(adLogs.getPlatformId());
			ads = adsRepo.save(ads);
		}
		adLogs.setAdsId(ads.getId());
		// ad log 数据不允许修改!
		return adLogsRepo.save(adLogs);
	}
}
