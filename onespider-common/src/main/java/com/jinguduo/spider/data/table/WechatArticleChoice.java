package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Entity
@Table(
    name = "wechat_article_choices",
    indexes = {@Index(name = "idx_keyword_day_o",  columnList="keyword,day,ordinal", unique = true)}
)
@Data
public class WechatArticleChoice implements Serializable {

    private static final long serialVersionUID = -3058580704762236087L;

    @Id
    @GeneratedValue
    private Integer id;
    
    @NotBlank
    @Column(nullable = false)
    private String keyword;
    private java.sql.Date day;
    
    @NotBlank
    @Column(nullable = false)
    private String title;
    
    @NotBlank
    @Column(nullable = false)
    private String url;
    
    private Integer readCount = 0;
    private Integer likeCount = 0;
    private Integer ordinal;

    private Integer greatest;

    private String postUser;

    private String originalId;

    //分数
    private Float score;

    private Date postTime;

    private String summary;

    private String code;

    private Integer type;

    private Integer relevanceId;


}
