package com.jinguduo.spider.cluster.spider;

import com.jinguduo.spider.data.table.SpiderSetting;

public interface SpiderSettingLoader {

	SpiderSetting load(Spider spider);
}
