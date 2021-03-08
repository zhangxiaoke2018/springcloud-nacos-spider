package com.jinguduo.spider.store;

import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.logging.LoggerConfig;

@Component
public class WeiboHotSearchTextCsvFileWriter extends CsvFileWriter {

	@Override
	protected LoggerConfig createLoggerConfig(Class<? extends Object> clazz) {
		LoggerConfig cfg = super.createLoggerConfig(clazz);
		cfg.setMaxFileSize("100MB");
		return cfg;
	}
}
