package com.jinguduo.spider.data.table.bookProject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lc on 2020/3/9
 */
@Data
@Table(name = "wechat_book_logs")
@Entity
public class WechatBookLogs {
    @Id
    @GeneratedValue
    private Integer id;
    private String bookCode;
    private Integer platformId;
    private String articleCode;
    private String title;
    private String summary;
    private Date articleTime;
    private String author;
}
