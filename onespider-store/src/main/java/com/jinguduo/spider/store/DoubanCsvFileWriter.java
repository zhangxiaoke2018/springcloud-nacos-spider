package com.jinguduo.spider.store;

import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.logging.LoggerConfig;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 30/10/2017 16:52
 */
@Component
public class DoubanCsvFileWriter extends CsvFileWriter {
    
    @Override
	protected LoggerConfig createLoggerConfig(Class<? extends Object> clazz) {
		LoggerConfig cfg = super.createLoggerConfig(clazz);
		cfg.setMaxFileSize("10MB");
		return cfg;
	}
}
