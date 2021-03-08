package com.jinguduo.spider;


import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import com.alibaba.nacos.api.config.ConfigService;


/**
 * 入口主类
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class StoreMainApplication  {

	public static void main(String[] args) {
		SpringApplication.run(StoreMainApplication.class, args);
	}



}
