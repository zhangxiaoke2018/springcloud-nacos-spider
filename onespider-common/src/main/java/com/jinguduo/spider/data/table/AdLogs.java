package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by jack on 2016/12/20.
 */

@Deprecated
@Entity
@Table(name = "ad_logs")
@Data
public class AdLogs {

    @Id
    @GeneratedValue
    private Long id;
    private String  fileName;
    private String  category;
    private Integer platformId; //平台Id
    private Integer adsId;
    @CreatedDate
    @DateTimeFormat
    private Date    createdAt = new Timestamp(System.currentTimeMillis()); //记录创建时间
    @LastModifiedBy
    @DateTimeFormat
    private Date    updatedAt = new Timestamp(System.currentTimeMillis()); //记录更新时间
    
    private String pageInfo;
    private String code;

}
