package com.jinguduo.spider.common.logging;

import java.nio.charset.Charset;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

public class Loggers {
	
	public static Logger newLogger(LoggerConfig cfg) {
		LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(ctx);
		encoder.setCharset(Charset.forName("UTF-8"));
		encoder.setPattern("%m%n");
		encoder.setImmediateFlush(cfg.getImmediateFlush());
		encoder.start();
		
		PatternLayout layout = (PatternLayout)encoder.getLayout();
		layout.setFileHeader(cfg.getFileHeader());  //getCsvSchemaHeader(clazz)

		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setContext(ctx);
		appender.setEncoder(encoder);
		appender.setAppend(cfg.getAppend());
		appender.setFile(cfg.getFilename());  //getFileName(clazz, path)
		appender.setBufferSize(cfg.getBufferSize());

		SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
		policy.setContext(ctx);
		policy.setParent(appender);
		policy.setFileNamePattern(cfg.getRollingFileNamePattern());  // getRollingFileNamePattern(clazz, path)
		if (cfg.getMaxHistory() > 0) {
		    policy.setMaxHistory(cfg.getMaxHistory());
        }
		policy.setMaxFileSize(FileSize.valueOf(cfg.getMaxFileSize()));
		policy.start();
		
		appender.setRollingPolicy(policy);
		appender.start();

		Logger logger = (Logger) LoggerFactory.getLogger(cfg.getLoggerName()); // clazz.getName() + ".CSV"
		logger.addAppender(appender);
		logger.setLevel(cfg.getLevel());
		logger.setAdditive(cfg.getAdditive());
		
		return logger;
	}

}
