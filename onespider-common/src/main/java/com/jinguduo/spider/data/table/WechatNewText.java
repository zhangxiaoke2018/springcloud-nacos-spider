package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;


@Entity
@Table(name = "wechat_new_texts")
@Data
public class WechatNewText implements Serializable {

    private static final long serialVersionUID = -1227269271982663285L;

    @Id
    @GeneratedValue
    private Long id;

    private String code;
    private String title;
    //简介
    private String summary;
    private String url;
    private String author;
    private Date articleTime;

    private Integer sort;

    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
}
