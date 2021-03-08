package com.jinguduo.spider.cluster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Job爬取状态
 * 
 */
@Data
public class JobState {

    private String id;  //job id
    
    @JsonProperty("t")
    private Long crawledTimestamp = 0L;  //抓取时间
    
    @JsonIgnore
    public static JobState of(Job job) {
        JobState state = new JobState();
        state.setId(job.getId());
        state.setCrawledTimestamp(System.currentTimeMillis());
        return state;
    }
}