package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import lombok.Data;

@Entity
@Table(name = "auto_find_logs")
@Data
public class AutoFindLogs implements Serializable{

    private static final long serialVersionUID = 1902275456746968689L;
    
    @Id
    @GeneratedValue
    private Integer id;
    
    private String name;
    private String category;
    private Integer platformId;
    private String url;
    private String code;
    @CreatedDate
    private Timestamp crawledTime = new Timestamp(System.currentTimeMillis());
    
    private Integer existJobStatus = 0;
    private Integer existJobId;
    private Integer existJobChecked;
    private Timestamp existJobTime;
    
    
    public AutoFindLogs() {
        super();
    }
    
    public AutoFindLogs(String name, String category, Integer platformId, String url, String code) {
        super();
        this.name = name;
        this.category = category;
        this.platformId = platformId;
        this.url = url;
        this.code = code;
    }

}
