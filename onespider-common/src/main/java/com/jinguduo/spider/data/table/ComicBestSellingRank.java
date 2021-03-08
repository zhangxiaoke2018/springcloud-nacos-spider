package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 31/07/2017 14:20
 */
@Data
@Table
@Entity
public class ComicBestSellingRank implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Integer platformId;

    private Date day;

    private Integer rank;

    private String name;

    private Integer riseStatus;

    private Integer rise;

}
