package com.jinguduo.spider.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


// XXX: 实验代码
@ConfigurationProperties(prefix="onespider.store.api")
public class StoreApiComponent {
    
    private String show = "show";
    private String showLog = "show_log";
    private String job = "job";
    private String seed = "seed";

}
