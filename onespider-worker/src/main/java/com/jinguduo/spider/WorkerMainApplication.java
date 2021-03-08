package com.jinguduo.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 入口主类
 * 
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
public class WorkerMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerMainApplication.class, args);
	}
}
