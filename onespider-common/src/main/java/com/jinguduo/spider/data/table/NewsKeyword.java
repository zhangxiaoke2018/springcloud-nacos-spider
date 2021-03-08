package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "news_keyword" )
public class NewsKeyword implements Serializable {


    private static final long serialVersionUID = -1771688998999529411L;
    @Id
    @GeneratedValue
    private Integer id;
    private Byte type;
    private String code;
    private String classify;
    private String keywords;

    @Column(updatable = false)
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    @Column(updatable = false)
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
