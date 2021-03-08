package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;


@Entity
@Table(name = "wechat_article_keyword_logs")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatArticleKeywordLog implements Serializable {

    private static final long serialVersionUID = 3662635954600534666L;

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private Integer articleCount = 0;
    
    private Integer articleLikeCount = 0;
    private Integer articleReadCount = 0;
    
    @NotNull
    private java.sql.Date day;
    
    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
    
    @NotBlank
    @Column(length = 64, nullable = false)
    private String keyword;

    private Integer type;

    private Integer relevanceId;

    private String code;
}
