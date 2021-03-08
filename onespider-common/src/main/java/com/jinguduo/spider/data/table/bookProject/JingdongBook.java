package com.jinguduo.spider.data.table.bookProject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by lc on 2020/4/2
 */
@Data
@Table(name = "jingdong_book")
@Entity
public class JingdongBook {
    @Id
    @GeneratedValue
    private Integer id;
    private String code;
    private Integer platformId;
    private String goodsId;
    private String url;
}
