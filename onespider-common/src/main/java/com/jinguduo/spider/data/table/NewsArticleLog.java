package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by csonezp on 2017/3/8.
 */
@Entity
@Table(name = "news_article_logs")
@Data
public class NewsArticleLog implements Serializable{

    private static final long serialVersionUID = 4556416394440488962L;

    @Id
    @GeneratedValue
    private Long id;
    private Integer showId;
    private String code;
    private String title;
    private String author;
    private String url;
    @Column(name = "`date`")
    private Date date;
}
