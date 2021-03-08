package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 24/03/2017 15:12
 */
@Entity
@Table
@Data
public class OpinionWords implements Serializable {

    private static final long serialVersionUID = -9048499181373950164L;

    @Id
    @GeneratedValue
    private Integer id;

    private String keyword = "";

    private String code = "";

    private Integer linkedId = 0;

    private Integer epi;

    //综艺的期数 20170108/10
    private String episode;

    private Integer totalEpi;

    @NotBlank
    private String category;

    @NotBlank
    private String subject;

    @Transient
    private String roleName;

    @NotBlank
    private String relatedKeyword;

    @Transient
    private String subjectFeature;

    @Transient
    private String relatedKeywordFeature;



    @Column(updatable = false)
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    @Column(updatable = false)
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

}
