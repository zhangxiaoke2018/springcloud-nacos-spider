package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 14/03/2017 11:00 AM
 */
@Table
@Entity
@Data
public class WechatBusiness implements Serializable {

    private static final long serialVersionUID = -3367689668017203656L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String name;

    @Column(name = "w_id")
    private String wechatId;

    @NotBlank
    private String originalId;

    private Boolean greatest = Boolean.FALSE;

}
