package com.guduo.dashboard.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.constant.WorkerCommand;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Worker {
    private String hostname;
    private String uuid;
    private String domain;
    private Integer ringIndex;
    private SpiderStatus status;
    private WorkerCommand command;
    private long timestamp;
}
