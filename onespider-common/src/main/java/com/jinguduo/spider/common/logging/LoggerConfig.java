package com.jinguduo.spider.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.FileSize;
import lombok.Data;

@Data
public class LoggerConfig {

    private String loggerName;
    private String fileHeader;
    private String filename;
    private String rollingFileNamePattern;
    
    private Level level = Level.INFO;
    private Boolean additive = false;
    private Boolean immediateFlush = false;
    private FileSize bufferSize = new FileSize(FileAppender.DEFAULT_BUFFER_SIZE);
    private Boolean append = true;
    private int maxHistory = 0;
    private String maxFileSize = "1GB";
}
