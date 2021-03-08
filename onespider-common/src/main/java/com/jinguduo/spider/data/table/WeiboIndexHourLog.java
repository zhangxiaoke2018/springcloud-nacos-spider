package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "weibo_index_hour_logs")
@Data
public class WeiboIndexHourLog implements Serializable {

    private static final long serialVersionUID = 8982449775912016026L;

    @Id
    @GeneratedValue
    private Long id;
    private String code;
    private Date hour;
    private Long indexCount;

    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    public WeiboIndexHourLog() {}

    public WeiboIndexHourLog(String code, Date hour, Long indexCount) {
        this.code = code;
        this.hour = hour;
        this.indexCount = indexCount;
    }
}
