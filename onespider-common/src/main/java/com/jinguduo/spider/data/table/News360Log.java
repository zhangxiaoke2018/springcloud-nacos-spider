package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;

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
 * @DATE 31/03/2017 17:15
 */
@Entity
@Data
@Table(name = "news_360_logs")
public class News360Log implements Serializable {

    private static final long serialVersionUID = -1115685624616317809L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String code;

    private Integer count;


    //创建时间
    @Column(updatable = false)
    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());

}
