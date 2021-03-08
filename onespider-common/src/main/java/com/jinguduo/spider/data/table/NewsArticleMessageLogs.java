package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by gaozl on 2020/10/23
 */
@Entity
@Table
@Data
public class NewsArticleMessageLogs implements Serializable{


    private static final long serialVersionUID = -6828263465415786558L;

    @Id
    @GeneratedValue
    private Long id;
    private String code;
    private String title;
    private String message;
    private String author;
    private String url;
    @Column(name = "`date`")
    private Date date;
}
