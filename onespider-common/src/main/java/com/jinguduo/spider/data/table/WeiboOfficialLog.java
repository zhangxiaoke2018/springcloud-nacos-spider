package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 01/04/2017 11:01
 */
@Data
@Entity
@Table(name = "weibo_official_logs")
public class WeiboOfficialLog implements Serializable {

    private static final long serialVersionUID = 4416337654842784564L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String code;

    @NotNull
    private Integer fansCount;

    @NotNull
    private Integer followCount;

    @NotNull
    private Integer postCount;

    private String weiboName;

    //创建时间
    @NotNull
    @Column(updatable = false)
    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());



}
