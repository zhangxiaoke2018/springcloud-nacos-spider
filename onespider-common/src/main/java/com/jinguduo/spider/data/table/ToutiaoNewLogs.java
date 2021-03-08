package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2017/5/15.
 */
@Entity
@Table(name = "news_toutiao_logs")
@Data
public class ToutiaoNewLogs implements Serializable {

    private static final long serialVersionUID = -1380154597042234293L;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "toutiao_id")
    private String toutiaoId;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "comments_count")
    private Integer commentsCount;

    @Column(name = "news_date")
    private Date newsDate;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_at", updatable = false)
    private Date updatedAt;

}
