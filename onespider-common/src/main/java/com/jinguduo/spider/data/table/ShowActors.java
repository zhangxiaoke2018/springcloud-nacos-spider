package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 03/07/2017 16:45
 */
@Entity
@Table
@Data
public class ShowActors implements Serializable {

    private static final long serialVersionUID = -3870446210790888690L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String code;

    private String actorNameCn;

    private String actorNameEn;

    private String celebrityCode;

    private String role;

    private Integer sequence;

    private String cover;



}
