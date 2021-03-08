package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @DATE 2018/10/12 3:53 PM
 */
@Data
@Entity
@Table
public class WechatArticleAccessory implements Serializable {

    private static final long serialVersionUID = -150504092335517253L;

    @Id
    @GeneratedValue
    private Long id;

    private String articleCode;

    private String biz;

    private String mid;

    private Integer idx;

    private Integer readCount;

    private Integer likeCount;

    private Integer rewardCount;

    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());

}
