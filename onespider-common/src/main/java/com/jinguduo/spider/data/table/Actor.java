package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by jack on 2016/12/20.
 */

@Entity
@Table(name = "actors")
@Data
public class Actor {

    @Id
    @GeneratedValue
    private int id;
    private String  name; //艺人name
    private String  code; //URL生成的code
    private Integer platformId; //平台Id
    private Integer linkedId; //艺人Id
    @CreatedDate
    @DateTimeFormat
    private Date    createdAt = new Timestamp(System.currentTimeMillis()); //记录创建时间
    @LastModifiedBy
    @DateTimeFormat
    private Date    updatedAt = new Timestamp(System.currentTimeMillis()); //记录更新时间
    private String url; //暂时用于展示媒体任务的url

}
