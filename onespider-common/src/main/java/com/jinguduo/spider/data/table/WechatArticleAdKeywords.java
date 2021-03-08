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
 * @TODO
 * @DATE 06/06/2017 09:48
 */
@Data
@Table
@Entity
public class WechatArticleAdKeywords implements Serializable {

    private static final long serialVersionUID = 607488088937478659L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String keyword;

}
