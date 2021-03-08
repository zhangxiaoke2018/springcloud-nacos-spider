package com.jinguduo.spider.store;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Column;
import com.jinguduo.spider.common.logging.LoggerConfig;
import com.jinguduo.spider.common.logging.Loggers;

import ch.qos.logback.classic.Logger;

@Component
@RefreshScope
public class CsvFileWriter implements InitializingBean, DisposableBean {

    @Value("${onespider.store.file.path}")
    private String storePath;
    
    private char columnSeparator = '\t';

    private CsvMapper mapper = new CsvMapper();
    
    private Map<String, Logger> loggerCache = new ConcurrentHashMap<>();
    
    private final Map<Class<? extends Object>, CsvSchema> schemaCache = new ConcurrentHashMap<>();
    
    private final Map<Class<? extends Object>, ObjectWriter> writerCache = new ConcurrentHashMap<>();

    public <T> void write(T o) throws IOException {
        getLogger(o.getClass()).info(stringify(o));
    }

    public <T> void write(Iterable<T> objects) throws IOException {
        for (T o : objects) {
            write(o);
        }
    }
    
    public <T> void write(T[] objects) throws IOException {
        for (T o : objects) {
            write(o);
        }
    }
    
    private final Pattern NEW_LINE_CHARACTOR = Pattern.compile("\r|\n");
    
    private <T> String stringify(T object) throws IOException {
        ObjectWriter writer = getObjectWriter(object.getClass());
        String s = writer.writeValueAsString(object);
        s = NEW_LINE_CHARACTOR.matcher(s).replaceAll(" ");
        return s;
    }
    
    private ObjectWriter getObjectWriter(Class<? extends Object> clazz) {
        ObjectWriter writer = writerCache.get(clazz);
        if (writer == null) {
            writer = mapper.writerFor(clazz).with(getCsvSchema(clazz));
            if (writer != null) {
                writerCache.put(clazz, writer);
            }
        }
        return writer;
    }
    
    private Logger getLogger(Class<? extends Object> clazz) {
        Logger logger = loggerCache.get(getLoggerName(clazz));
        if (logger == null) {
            logger = Loggers.newLogger(createLoggerConfig(clazz));
            loggerCache.put(getLoggerName(clazz), logger);
        }
        return logger;
    }
    
    protected LoggerConfig createLoggerConfig(Class<? extends Object> clazz) {
        LoggerConfig cfg = new LoggerConfig();
        
        cfg.setLoggerName(getLoggerName(clazz));
        cfg.setFilename(getFileName(clazz, storePath));
        cfg.setRollingFileNamePattern(getRollingFileNamePattern(clazz, storePath));
        cfg.setFileHeader(getCsvSchemaHeader(clazz));
        
        return cfg;
    }

    private String getLoggerName(Class<? extends Object> clazz) {
        return clazz.getName() + ".CSV";
    }

    private CsvSchema getCsvSchema(Class<? extends Object> clazz) {
        CsvSchema schema = schemaCache.get(clazz);
        if (schema == null) {
            // Schema from POJO (usually has @JsonPropertyOrder annotation)
            schema = mapper.schemaFor(clazz);
            schema = schema.withColumnSeparator(columnSeparator).withNullValue("").withLineSeparator("");
            schemaCache.put(clazz, schema);
        }
        return schema;
    }
    
    private String getCsvSchemaHeader(Class<? extends Object> clazz) {
        CsvSchema schema = getCsvSchema(clazz);
        StringBuilder sb = new StringBuilder();
        sb.append('#');  //comment
        boolean sep = false;
        for (Column column : schema) {
            if (sep) {
                sb.append(columnSeparator);
            }
            sb.append(column.getName());
            sep = true;
        }
        return sb.toString();
    }
    
    private String getFileName(Class<? extends Object> clazz, String path) {
        String name = clazz.getSimpleName().toLowerCase();
        if (path.endsWith("/")) {
            return path + name + ".log";
        } else {
            return path + "/" + name + ".log";
        }
    }

    private String rollingFileNamePattern = ".%d{yyyyMMdd}.%i.gz";
    private String getRollingFileNamePattern(Class<? extends Object> clazz, String path) {
        return getFileName(clazz, path) + rollingFileNamePattern;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(storePath, "The 'storePath' may not be null");
    }

	@Override
	public void destroy() throws Exception {
		for (Logger l : loggerCache.values()) {
			l.detachAndStopAllAppenders();
		}
	}
	
	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}
}
