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

@Deprecated
@Entity
@Table(name = "ads")
@Data
public class Ads {

    @Id
    @GeneratedValue
    private Integer id;
    private String  fileName;
    private String  name;
    private Integer platformId; //平台Id
    private String category;
    @CreatedDate
    @DateTimeFormat
    private Date  createdAt = new Timestamp(System.currentTimeMillis());
    @LastModifiedBy
    @DateTimeFormat
    private Date  updatedAt = new Timestamp(System.currentTimeMillis());

}
