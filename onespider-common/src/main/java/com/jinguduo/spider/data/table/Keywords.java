package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;
import org.springframework.data.annotation.Persistent;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@Entity
@Data
@Table(name = "keywords")
public class Keywords implements Serializable {

    private static final long serialVersionUID = 267708383533502057L;

    @Id
    @GeneratedValue
    private Integer id;

    private String type = "SHOW";

    private Integer greatest = 0;

    @NotBlank
    private String keyword;

    private Integer category;

    private String code;

    private Boolean deleted = Boolean.FALSE;
    
    private Boolean modified = Boolean.FALSE;

    private String relatedKeyword;

    @Transient
    private Integer LinkedId;

    @Transient
    private Collection<RKeywordsLinked> linkeds;

    @Column(updatable = false)
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    
    @Column(nullable = false)
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());




}
