package com.jinguduo.spider.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class IoResourceHelper {

	
	public static String readResourceContent(String classpath) {
		return readResourceContent(new ClassPathResource(classpath));
	}
	
	public static String readResourceContent(Resource resource) {
		StringBuilder buf = new StringBuilder();
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				buf.append(line);
			}
			
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception ignore) {
				}
			}
		}
		return buf.toString();
	}
}
