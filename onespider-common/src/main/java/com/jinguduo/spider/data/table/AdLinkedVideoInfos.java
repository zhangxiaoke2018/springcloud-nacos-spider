package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;


@Deprecated
@Entity
@Table(name = "ad_linked_video_infos")
@Data
public class AdLinkedVideoInfos {

    @Id
    @GeneratedValue
    private Long id;
    private Integer platformId; //平台Id
    private String pageUrl;
    private String code;
    private Long playCount;
    private String title;
    private String category;
    private Integer seconds;
    
    @Column(updatable = false)
    @CreatedDate
    @DateTimeFormat
    private Date    createdAt = new Timestamp(System.currentTimeMillis()); //记录创建时间
    @Column(updatable = false)
	@LastModifiedDate
    @DateTimeFormat
    private Date    updatedAt = new Timestamp(System.currentTimeMillis()); //记录更新时间

}
