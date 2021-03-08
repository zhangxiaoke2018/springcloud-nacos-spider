package com.jinguduo.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;


/**
 * 入口主类
 * 
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.jinguduo.spider.access")
public class SupervisorMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupervisorMainApplication.class, args);
	}
}
