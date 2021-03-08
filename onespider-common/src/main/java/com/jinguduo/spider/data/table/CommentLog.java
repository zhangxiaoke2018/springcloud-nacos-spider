package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "comment_logs")
@Data
public class CommentLog implements Serializable {

    @Id
    @GeneratedValue
    private Long id;
    private Integer jobId;//实际表中job_id并没有用到，为了区别爱奇艺的泡泡评论而加
    private Integer showId;
    private Integer platformId;
    private Integer seedId;
    private Integer commentCount;
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
    @Column(length = 100)
    private String code;

    public CommentLog() {
    }

    public CommentLog( Integer commentCount ) {
        this.commentCount = commentCount;
    }
}
