package com.jinguduo.spider.data.table.bookProject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lc on 2020/1/17
 */
@Data
@Table(name = "jianshu_book_logs")
@Entity
public class JianshuBookLogs {

    @Id
    @GeneratedValue
    private Integer id;
    private String keyword;
    private String code;
    private String title;
    private Integer likes;
    private Integer views;
    private Integer comments;
    private Date shareTime;

}
