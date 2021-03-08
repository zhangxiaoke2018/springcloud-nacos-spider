package com.jinguduo.spider.store;

import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.logging.LoggerConfig;

import ch.qos.logback.core.util.FileSize;


@Component
public class DouyinVideoStatisticCsvFileWriter extends CsvFileWriter {
	
	private final static String BUFFER_SIZE = "64KB";

	@Override
	protected LoggerConfig createLoggerConfig(Class<? extends Object> clazz) {
		LoggerConfig cfg = super.createLoggerConfig(clazz);
		cfg.setMaxFileSize("100MB");
		cfg.setBufferSize(FileSize.valueOf(BUFFER_SIZE));
		return cfg;
	}
}
