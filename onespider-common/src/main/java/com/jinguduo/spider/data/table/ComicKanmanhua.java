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
@Table(name = "comic_kanmanhua")
@Entity
public class ComicKanmanhua implements Serializable {


    private static final long serialVersionUID = -2852616398349116324L;
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Date day;

    private Integer shoucang;

    private Long renqi;

    private Integer dashang;

    private Integer gift;

    private Integer yuepiao;

    private Integer tuijian;

    private Float pingfen;

    private Integer share;

    private Integer commentCount;


}
