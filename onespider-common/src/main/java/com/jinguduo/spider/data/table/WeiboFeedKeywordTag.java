package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "weibo_feed_keyword_tags")
@Data
public class WeiboFeedKeywordTag implements Serializable {

    private static final long serialVersionUID = -6377225307358138796L;

    @Id
    @GeneratedValue
    private Integer id;

    private String keyword;
    private String tag;
    private String code;
    private Date day;


    @Column(updatable = false)
    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());


}
