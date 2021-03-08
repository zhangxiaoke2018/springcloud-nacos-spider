package com.guduo.dashboard;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@SpringBootApplication
@NacosPropertySource(dataId = "spider-dashboard", autoRefreshed = true)
public class DashboardApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }

    @NacosInjected
    private NamingService namingService;

    @RequestMapping("/keepalived")
    public String keepalived() {
        return "OK";
    }


    @NacosValue(value = "${spring.application.name}",autoRefreshed = true)
    private String applicationName;

    @NacosValue(value = "${server.port}",autoRefreshed = true)
    private Integer serverPort;

    @Override
    public void run(String... args) throws  Exception{
        //通过Naming服务注册实例到注册中心
        namingService.registerInstance(applicationName,"127.0.0.1",serverPort);
    }
}
