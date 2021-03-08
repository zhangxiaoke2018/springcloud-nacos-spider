package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@Table(name = "weibo_feed_keyword_logs")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeiboFeedKeywordLog implements Serializable {

    private static final long serialVersionUID = 7550596372359903533L;

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private Integer feedCount;
    
    @Column(nullable = false)
    private Integer forwardCount;
    
    @Column(nullable = false)
    private Integer commentCount;
    
    @Column(nullable = false)
    private Integer likeCount;
    
    @NotNull
    private java.sql.Date day;
    
    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
    
    @NotBlank
    @Column(length = 64, nullable = false)
    private String keyword;

    private Integer type;

    private Integer relevanceId;
}
