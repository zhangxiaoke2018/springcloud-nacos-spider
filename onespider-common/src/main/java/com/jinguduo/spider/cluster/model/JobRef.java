package com.jinguduo.spider.cluster.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


// XXX: 实验代码
@Data
public class JobRef {

    private String id;
    
    @JsonProperty("hc")
    private Integer hashCode;
    
    @JsonIgnore  // @JsonProperty("sc")
    private Integer statusCode;
    
    @JsonIgnore  //@JsonProperty("ctt")
    private Timestamp crawledTimestamp;
    
    public static JobRef of(Job job) {
        JobRef ref = new JobRef();
        ref.setId(job.getId());
        ref.setHashCode(job.hashCode());
        return ref;
    }
}
