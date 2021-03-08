package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
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
 * @DATE 23/05/2017 14:00
 */
@Data
@Entity
@Table(name = "crab_settings")
public class CrabSettings implements Serializable {

    private static final long serialVersionUID = -6587039596762315880L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String code;

    @NotBlank
    private String url;

    @NotBlank
    private String params;

    @Column(name="`desc`")
    private String desc;

    @Column(updatable = false)
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    @Column(updatable = false)
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());


}
