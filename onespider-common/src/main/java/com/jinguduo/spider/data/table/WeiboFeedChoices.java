package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/03/2017 17:49
 */
@Data
@Entity
@Table
public class WeiboFeedChoices implements Serializable {

    private static final long serialVersionUID = -6509976440338303228L;

    @Id
    @GeneratedValue
    private Integer id;

    private String keyword;

    private Integer forwardCount;

    private Integer commentCount;

    private Integer likeCount;

    private Timestamp postTime;

    private String nickName;

    private String content;

    private Integer type;

    private Integer relevanceId;

}
