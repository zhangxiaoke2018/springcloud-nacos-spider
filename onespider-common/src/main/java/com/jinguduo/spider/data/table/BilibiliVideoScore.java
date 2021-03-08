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
@Table(name="bilibili_score_count")
@Data
public class BilibiliVideoScore implements Serializable {

    private static final long serialVersionUID = -7426455334066269030L;

    @Id
    @GeneratedValue
    public Long id;

    public Double score;

    public String code;


    public Integer scoreNumber;


    public Date day;



    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());






}
