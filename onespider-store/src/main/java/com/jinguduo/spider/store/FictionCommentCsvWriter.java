package com.jinguduo.spider.store;

import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.logging.LoggerConfig;

import ch.qos.logback.core.util.FileSize;
@Component
public class FictionCommentCsvWriter extends CsvFileWriter {
private final static String BUFFER_SIZE = "32KB";
	
    @Override
	protected LoggerConfig createLoggerConfig(Class<? extends Object> clazz) {
		LoggerConfig cfg = super.createLoggerConfig(clazz);
		cfg.setMaxFileSize("400MB");
		cfg.setBufferSize(FileSize.valueOf(BUFFER_SIZE));
		return cfg;
	}
}
