package com.jinguduo.spider.cluster.spider;

import com.jinguduo.spider.data.table.SpiderSetting;

public class DefaultSpiderSettingLoader implements SpiderSettingLoader {

	@Override
	public SpiderSetting load(Spider spider) {
		SpiderSetting spiderSetting = new SpiderSetting();
		spiderSetting.setDomain(spider.getSite().getDomain());
		
		return spiderSetting;
	}

}
