package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Table(name = "weibo_tag_logs")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeiboTagLog implements Serializable {

    private static final long serialVersionUID = 2569938393405378406L;

    @Id
    @GeneratedValue
    private Integer id;
    
    @NotBlank
    @Column(length = 64, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private Long readCount;  // 阅读数
    
    @Column(nullable = false)
    private Integer feedCount;  // 讨论数
    
    @Column(nullable = false)
    private Integer followCount;  // 粉丝数
    
    @Column(length = 64)
    private String parentCode;

    @Column(length = 64)
    private String keyword;

    private Date day;


    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
}
