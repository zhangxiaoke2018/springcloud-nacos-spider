package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 20/04/2017 18:46
 */
@Data
@Entity
@Table
public class TiebaArticleLogs implements Serializable {

    private static final long serialVersionUID = -318459101799532821L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String code;

    @NotBlank
    private String title;

    @NotNull
    private Integer repNum;

    @NotNull
    private String url;




}
