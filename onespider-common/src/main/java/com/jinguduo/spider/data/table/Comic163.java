package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 04/08/2017 14:00
 */
@Data
@Entity
@Table(name = "comic_163")
public class Comic163 implements Serializable {

    private static final long serialVersionUID = 2319522011387324343L;

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Long totalPlayCount;

    private Integer tucaoCount;

    private Integer commentCount;



}
